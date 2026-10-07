---
layout: page
title: Documentation guide
---

## Keep the docs current

* **User Guide:** Explain commands that users can run now, with short examples and expected results.
* **Developer Guide:** Explain the design, requirements and tests.
* **Student model and scope:** Keep shared field rules and feature ownership in one place.
* **Google Doc and story Sheet:** Keep active plans consistent with the repository. Preserve story IDs and priorities.

Use short sentences and familiar words. Put the action first. Label planned features clearly, and explain technical terms where readers need them. Keep exact command syntax, validation rules and error messages intact.

PR descriptions should explain the problem, the change and the checks performed. State merge dependencies first. Include limits that affect review; leave out the history of drafting and reviewing the PR.

See the [Google documentation style guide](https://developers.google.com/style) and [Markdown coding standard](https://se-education.org/guides/conventions/markdown.html).

## Maintain the website

[Jekyll](https://jekyllrb.com/) builds the website from `docs/`. Use `docs/_config.yml` for site settings and navigation. Styles are in `docs/_sass`; `_base.scss` also contains the PDF header label.

* [Set up and use Jekyll](https://se-education.org/guides/tutorials/jekyll.html)
* [Enable soft wrapping in IntelliJ](https://se-education.org/guides/tutorials/intellijUsefulSettings.html#enabling-soft-wrapping)
* [Edit PlantUML diagrams](https://se-education.org/guides/tutorials/plantUml.html), stored in `docs/diagrams`
* [Save web pages as PDFs](https://se-education.org/guides/tutorials/savingPdf.html)
