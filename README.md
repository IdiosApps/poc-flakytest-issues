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
  - Then have two failing tests; two issues should be created/updated
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

---
---

# Experimenting

## Creating issues
https://github.com/marketplace/actions/create-an-issue Looks really neat:

- Create issues with a template
- Generate date, be passed e.g. title from ENV
- Can search existing issues (by exact title match), and update them

I don't feel like I need to look further until giving this one a good try!

## Checking for failed tests

Can we extract:
- the test name?
- the test path?
- the expected/actual values?

Looking at some Actions, it seems JUnit will output an XML report anyway.

- https://github.com/marketplace/actions/junit-report-action
  - Uses that XML output to annotate PRs with failures (handy! but not for this poc)
  - Gives a count of failed cases with an output: `outputs.failed`
    - This gives me hope that there will be more Actions that parse the XML - but we'll have to search more to see if one can give us the information straight back to the Action.
- https://github.com/marketplace/actions/test-reporter
  - For our purposes, looks similar to the above
- Parse XML and give results for the matching xpath

I think we had best generate some XML to see what we're playing with!

### Make some passing/failing JUnit tests

I used IntelliJ's https://start.spring.io/ integration to make a Maven project, and copied files over.

Running `.\gradlew.bat test` creeates `build/test-results/test/TEST-com.example.demo.DemoApplicationTests.xml`.

For each test file, an .xml is generated - maybe there is an arg to collapse results into one file.
Here's how it looks:

```xml
<!-- A failing testcase has a failure block inside -->
<testcase name="iFail()" classname="com.example.demo.DemoApplicationTests" time="0.286">
  <failure message="java.lang.AssertionError" type="java.lang.AssertionError">java.lang.AssertionError
    at com.example.demo.DemoApplicationTests.iFail(DemoApplicationTests.java:15)
    <!-- 99 lines of useless stack trace -->
  </failure>
</testcase>
<!-- The passing test below closes itself -->
<testcase name="contextLoads()" classname="com.example.demo.DemoApplicationTests" time="0.002"/>
```

Let's see what happens if we add proper assertions:

```xml
<testcase name="iFail()" classname="com.example.demo.DemoApplicationTests" time="0.286">
  <!-- We get a pretty descriptive failure message now! -->
  <failure message="org.opentest4j.AssertionFailedError: expected: &lt;0&gt; but was: &lt;1&gt;" type="org.opentest4j.AssertionFailedError">org.opentest4j.AssertionFailedError: expected: &lt;0&gt; but was: &lt;1&gt;
  at app//org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:55)
  at app//org.junit.jupiter.api.AssertionUtils.failNotEqual(AssertionUtils.java:62)
  at app//org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:150)
  at app//org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:145)
  at app//org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:527)
  <!-- possibly useful line, pointing to the line where the error happened  -->
  at app//com.example.demo.DemoApplicationTests.iFail(DemoApplicationTests.java:16)
  <!-- boring stack  -->
  </failure>
</testcase>
```

At this point, I'm thinking it would be great if an Action could parse this and output something like:

```
Errors = [Error]
Error(testName, className, failureMessage)
```

So we could do something like this:

```
action.output.errors . map ( error => {
  issueTitle = "${error.testName} in ${error.className}"
  issueBody = error.failureMessage
  makeIssueFor(issueTitle, issueBody)
})
```

If we think a test might be flaky but for multiple different reasons, the title could include the reason too (or maybe a short hash if there is a character limit!).

[Okay, I'll admit I got curious and went on another tangent here. 1024 characters is allowed, so (for now!) lots of info seems fine!](https://github.com/IdiosApps/poc-flakytest-issues/issues/2#issuecomment-1412955980)

Oh, and what about our build tool giving us a single XML file to work with?

According to https://github.com/marketplace/actions/junit-report-action, each file is scanned separately: `**/junit-reports/TEST-*.xml`

Whatever we come up with should check each test output - no funky merging necessary.

## Failures in XML -> GHA -> Issues

For the XML -> GHA -> Issues chain, I'm not sure if it'll be easier to do it all in one action.
For a start, we can quickly understand and play with extracting the XML data in a GitHub Action by examining an existing Action around JUnit XML flakiness.

To try and leverage the create-issue action we've already tried, we can try and make an Action that outputs some text in our expected format and see how we can munge it.



# OK, but I can't make these flaky tests not flaky

The observability this POC gives you could still be valuable - but if you can't fix your flaky tests then I'd recommend reading around what other companies have done to address these kinds of tests:

- https://www.uber.com/en-GB/blog/handling-flaky-tests-java
  - they did ...
- https://github.blog/2020-12-16-reducing-flaky-builds-by-18x
  - they did ...
- https://gradle.com/blog/do-you-regularly-schedule-flaky-test-days/
  - dedicate some time to figuring *something* out
    - ad-hoc is OK
  - Gradle Enterprise can measure flakiness for you
    - > “What gets measured gets improved.” ~ Peter Drucker, Business Luminary
- feel free to link more, and give a short summary


# Miscellaneous notes
- For Scala
  - For mill
    - https://github.com/vic/mill-test-junit-report
      - JSON test output -> JUnit XML
  - for sbt
    - https://www.scala-sbt.org/1.x/docs/Testing.html#Test+Reports 
      - JUnit XML reports by default

# FAQ
- > Won't there be a bunch of issues from failed tests as people develop PRs?
  - For now, I'll build the POC to run manually/on CRON/on push to main
  - So, any failing tests on `main` must have passed at least once (assuming PR checks include tests) - i.e. only flaky tests will be recorded