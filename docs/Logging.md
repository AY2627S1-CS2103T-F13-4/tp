---
layout: page
title: Logging guide
---

The app uses `java.util.logging`. `LogsCenter` sets the log level and writes to the console and rotating `.log` files.

* Get a logger with `LogsCenter.getLogger(YourClass.class)`.
* Change `LogsCenter.LOG_LEVEL` to adjust detail; the default is `INFO`.
* Follow the [Java logging conventions](https://se-education.org/guides/conventions/java/logging.html) when choosing a message level.

The inherited `LogicManager` logs raw commands. Use sample contact details during testing; the data-protection requirement in the Developer Guide is a target, not a claim that these logs are already redacted.
