# poc-flakytest-issues

A POC that uses GitHub Actions to:
1. Add new flaky tests as GitHub Issues
2. Increment a counter (in title/descrition - or add a comment, if much simpler) to track frequency

In early stages, more data can be gathered by re-running the whole test suite e.g. hourly with a CRON trigger, for one or two days.

---

# What tech might work well

GitHub offers a lot of tooling that could help make this achieveable:
- Vast marketplace of Actions
- Issues/Discussion etc. give places to track content (tangent: utteranc.es and similar apps use this kind of datastore for comments on websites, and that's neat)
- Various APIs and CLIs, which can be used in Actions too
- A clean UI to integrate it all together

# What might the simplest POC look like?

- Testing framework, language, etc.
  - Java, JUnit, JSON output for easy parsing. It's a pretty popular setup, so could be useful too. PyTest seems like another simple option.
- Tests
  - At first, have one passing test and one failing test
  - On every failed test, an Issue should either 1. be created, or 2. be incremented
- Actions
  - Ideally, use a combination of off-the-shelf Actions
    - Writing our own Action may be required, but we may not have to reinvent (and maintain!) the wheel 
  
  
# Assumptions
- We can use GitHub Issues or Discussion
  - day-to-day dev tickets aren't done in either Issues or Discussion
  - or, labels can be applied and filtered out so that the flaky tests don't spam day-to-day work yet can all be confused on separately



---

# What's out there already?

## BuildPulse

[BuildPulse](https://buildpulse.io/) looks neat - it seems to:
- Detect which test was flaky (test name, expected/vs actual, etc.)
- Give "last seen" date, and a frequency graph over last 2 weeks
- Give total disruption count

However, it costs $100 a month (arguably, it could save more dev time than the monetary cost - so that might be a good price!.
