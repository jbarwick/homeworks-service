# Lutron integration protocol

The developer guide for this exporter is the Lutron Integration Protocol, revision AB, 22 August 2019. It shipped with Lutron Designer 15.13.1.

Local copy:

`H:\Documents\Lutron\C__ProgramData_Lutron_Lutron Designer_Lutron Designer 15.13.1_Lutron Designer__IntegrationInfo.pdf`

That PDF stays on the NAS. It is Lutron's manual, so it is not converted into this repository.

Printed pages that matter for HomeWorks QS:

- About integration, pages 1–3. Operation characters, command shape, delays.
- Integrator's reference, pages 4–15. DEVICE, OUTPUT, and monitoring summaries.
- HomeWorks QS, pages 46–50 and the system commands that follow.
- Device chapters from page 74. Keypads, dimmers, and shades.

## What this exporter sends

Messages are ASCII fields separated by commas and ended with CR LF. The processor prompts this code looks for are `login:`, `password:`, `QNET?`, and `QNET>`.

| Operation | Character | This repo |
| --- | --- | --- |
| Execute | `#` | Not used. The exporter does not set levels or press buttons. |
| Query | `?` | `?OUTPUT,<integration id>,1` for zone level. `?DEVICE,<integration id>,<component>,9` for LED state. |
| Report | `~` | Parsed from `~OUTPUT` and `~DEVICE` replies. |

`FIRMWAREREV` is sent after login. The reply lines feed firmware version, design version, and revision. Design XML comes from `http://<address>/DbXmlInfo.xml`, which is outside this telnet protocol.

Integration IDs in those commands are the `IntegrationID` values in the design XML, not the names shown in Designer.

Each `/metrics` call opens its own telnet session and closes it before the response returns. A session that cannot log in, times out, or dies while the processor is publishing a new design is logged and discarded. Gauge values from the last finished scrape stay in place. The next `/metrics` call tries again. This exporter does not keep a telnet session open between scrapes.
