#!/usr/bin/env python3
"""Render DG constructor sequences with centered creation arrows and joined activations.

Requires Java, PlantUML 1.2025.10, Python 3, Node.js and sharp. Install sharp
locally with: npm install --prefix _temp/uml-render sharp
"""

import argparse
import os
from pathlib import Path
import re
import struct
import subprocess
import tempfile
from xml.parsers import expat
from xml.sax.saxutils import escape
import zlib


def fix_constructor_geometry(svg_text):
    """Center creation heads and join their activations, preserving return endpoints."""
    source = svg_text.encode("utf-8")
    nodes, stack = [], []
    parser = expat.ParserCreate(namespace_separator="}")
    start_tag = re.compile(br'''<(?:[^>"']|"[^"]*"|'[^']*')*>''')

    def start(name, attrs):
        offset = parser.CurrentByteIndex
        match = start_tag.match(source, offset)
        if match is None:
            raise ValueError("Cannot locate SVG start tag")
        node = {"name": name.rsplit("}", 1)[-1], "attrs": attrs,
                "children": [], "parent": stack[-1] if stack else None,
                "start": offset, "end": match.end()}
        if stack:
            stack[-1]["children"].append(node)
        nodes.append(node)
        stack.append(node)

    parser.StartElementHandler = start
    parser.EndElementHandler = lambda _name: stack.pop()
    parser.Parse(source, True)

    def number(node, key):
        return float(node["attrs"][key])

    def is_head(node):
        return (node["name"] == "rect" and number(node, "width") > 20
                and node["attrs"].get("fill", "none").upper() not in
                {"NONE", "#FFFFFF", "WHITE", "#000000"})

    bars = [node for node in nodes if node["name"] == "rect"
            and node["attrs"].get("fill", "").upper() == "#FFFFFF"
            and abs(number(node, "width") - 10) < 0.001]
    changes = {}
    count = 0

    def change(node, **attributes):
        changes.setdefault(node["start"], (node, {}))[1].update(attributes)

    for message in nodes:
        if message["name"] != "g" or "message" not in message["attrs"].get("class", "").split():
            continue
        # PlantUML puts creation heads inside or immediately after their message.
        siblings = message["children"]
        heads = [node for node in siblings if is_head(node)]
        if len(heads) > 1:
            raise ValueError("Ambiguous constructor head rectangles")
        head = heads[0] if heads else None
        if head is None:
            siblings = message["parent"]["children"]
            position = next(i for i, node in enumerate(siblings) if node is message) + 1
            if position == len(siblings) or not is_head(siblings[position]):
                continue
            head = siblings[position]
        line = next((node for node in message["children"]
                     if node["name"] == "line" and
                     abs(number(node, "y1") - number(node, "y2")) < 0.001), None)
        if line is None:
            raise ValueError("Constructor has no horizontal arrow")
        arrow_y = number(line, "y1")
        height = number(head, "height")
        new_y = arrow_y - height / 2
        new_bottom = new_y + height
        delta = new_y - number(head, "y")
        bar_x = number(head, "x") + number(head, "width") / 2 - 5
        candidates = [bar for bar in bars if abs(number(bar, "x") - bar_x) <= 2
                      and number(bar, "y") >= arrow_y - 0.001]
        if not candidates:
            raise ValueError("Constructor has no matching activation rectangle")
        bar = min(candidates, key=lambda node: number(node, "y"))
        old_y, old_height = number(bar, "y"), number(bar, "height")
        earliest = [node for node in candidates if abs(number(node, "y") - old_y) < 0.001]
        if any(abs(number(node, "x") - number(bar, "x")) >= 0.001
               or abs(number(node, "height") - old_height) >= 0.001 for node in earliest):
            raise ValueError("Ambiguous constructor activation rectangles")
        return_y = old_y + old_height
        new_height = return_y - new_bottom
        if new_height < 1:
            raise ValueError("Constructor activation needs more duration before its return")

        change(head, y=new_y)
        position = next(i for i, node in enumerate(siblings) if node is head) + 1
        while position < len(siblings) and siblings[position]["name"] == "text":
            text = siblings[position]
            change(text, y=number(text, "y") + delta)
            position += 1
        for duplicate in candidates:
            if (abs(number(duplicate, "x") - number(bar, "x")) < 0.001
                    and abs(number(duplicate, "y") - old_y) < 0.001
                    and abs(number(duplicate, "height") - old_height) < 0.001):
                change(duplicate, y=new_bottom, height=new_height)
        count += 1

    # Patch attributes without rewriting SVG namespaces or embedded source data.
    output = source
    for _offset, (node, attributes) in sorted(changes.items(), reverse=True):
        tag = source[node["start"]:node["end"]]
        for key, value in attributes.items():
            pattern = re.compile(rb'(\s' + key.encode("ascii") + rb'=)(["\'])(.*?)(\2)')
            formatted = ("%.6f" % value).rstrip("0").rstrip(".").encode("ascii")
            tag, replacements = pattern.subn(
                lambda match: match[1] + match[2] + formatted + match[4], tag)
            if replacements != 1:
                raise ValueError("Cannot update SVG attribute " + key)
        output = output[:node["start"]] + tag + output[node["end"]:]
    return output.decode("utf-8"), count


def with_source_metadata(png, source):
    """Embed the editable PlantUML source in a standard compressed PNG text chunk."""
    if png[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("Rasterizer did not return a PNG")
    content = b"plantuml\0\x01\0\0\0" + zlib.compress(source.encode("utf-8"))
    chunk = b"iTXt" + content
    chunk = struct.pack(">I", len(content)) + chunk + struct.pack(">I", zlib.crc32(chunk))
    if png[-8:-4] != b"IEND":
        raise ValueError("PNG has no final IEND chunk")
    return png[:-12] + chunk + png[-12:]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--plantuml-jar", type=Path, required=True)
    parser.add_argument("--node", default="node", help="Node.js executable")
    args = parser.parse_args()
    diagrams = Path(__file__).resolve().parent
    repository = diagrams.parent.parent
    guide = (diagrams.parent / "DeveloperGuide.md").read_text(encoding="utf-8")
    sources = []
    for name in dict.fromkeys(re.findall(r"images/([\w-]+)\.png", guide)):
        path = diagrams / f"{name}.puml"
        if path.exists() and re.search(r"(?m)^\s*create\s+", path.read_text(encoding="utf-8")):
            sources.append(path)
    if not sources:
        raise ValueError("No constructor sequences found in the Developer Guide")
    jar = args.plantuml_jar.resolve(strict=True)
    rasterize = """
const sharp = require(require.resolve('sharp', {
    paths: [process.cwd(), process.cwd() + '/_temp/uml-render']
}));
sharp(process.argv[1], {density: 72}).flatten({background: '#ffffff'})
    .png().toFile(process.argv[2]).catch(error => {
        console.error(error.message);
        process.exitCode = 1;
    });
"""
    with tempfile.TemporaryDirectory(prefix="studentbook-uml-") as temporary:
        output = Path(temporary)
        raster_environment = os.environ.copy()
        if os.name == "nt":
            # Give librsvg the same Windows fonts as Java and a writable cache.
            fonts = (Path(os.environ.get("WINDIR", "C:/Windows")) / "Fonts").as_posix()
            cache = output / "font-cache"
            cache.mkdir()
            config = output / "fonts.conf"
            config.write_text(
                "<?xml version='1.0'?><fontconfig>"
                f"<dir>{escape(fonts)}</dir><cachedir>{escape(cache.as_posix())}</cachedir>"
                "<alias><family>sans-serif</family><prefer><family>Arial</family></prefer></alias>"
                "</fontconfig>", encoding="utf-8")
            raster_environment["FONTCONFIG_FILE"] = str(config)
        subprocess.run([
            "java", "-Djava.awt.headless=true", "-jar", str(jar),
            "-charset", "UTF-8", "-failfast2", "-tsvg", "-o", str(output),
            *map(str, sources),
        ], cwd=diagrams, check=True)
        rendered = []
        for source in sources:
            svg = output / f"{source.stem}.svg"
            corrected, count = fix_constructor_geometry(svg.read_text(encoding="utf-8"))
            source_text = source.read_text(encoding="utf-8")
            expected = len(re.findall(r"(?m)^\s*create\s+", source_text))
            if count != expected:
                raise ValueError(f"Expected {expected} constructors in {source.name}, found {count}")
            svg.write_text(corrected, encoding="utf-8")
            png = output / f"{source.stem}.png"
            subprocess.run([
                args.node, "-e", rasterize, str(svg), str(png),
            ], cwd=repository, env=raster_environment, check=True)
            rendered.append((source, count, with_source_metadata(
                png.read_bytes(), source_text)))
        for source, count, png in rendered:
            (diagrams.parent / "images" / f"{source.stem}.png").write_bytes(png)
            print(f"{source.stem}.png: aligned {count} constructor(s)")


if __name__ == "__main__":
    main()
