ℹ **Check out our new [performance test app that includes ObjectBox](https://github.com/objectbox/objectbox-performance).**

Android Database Performance Benchmark
======================================
This project evaluates Android databases and related persistence solutions. It tests SQLite, SQLite in combination with several ORMs, and alternative database options.

[![Build Status](https://travis-ci.org/greenrobot/android-database-performance.svg?branch=master)](https://travis-ci.org/greenrobot/android-database-performance)

## Usage

To run the Android instrumentation tests make sure to set `RUN_PERFORMANCE_TESTS` in [`Common\build.gradle`][1] to `true`.

To run with Android Studio, create a new `Android Instrumented Tests` run configuration. Specify the desired `PerfTest<x>` class and a test method (see [`BasePerfTestCase`][2]).


More Open Source by greenrobot
==============================
[__ObjectBox__](https://github.com/objectbox/objectbox-java) is a new and fast object-oriented database for mobile devices.

[__greenDAO__](https://github.com/greenrobot/greenDAO) is an ORM optimized for Android: it maps database tables to Java objects and uses code generation for optimal speed.

[__EventBus__](https://github.com/greenrobot/EventBus) is a central publish/subscribe bus for Android with optional delivery threads, priorities, and sticky events. A great tool to decouple components (e.g. Activities, Fragments, logic components) from each other.

[__greenrobot-common__](https://github.com/greenrobot/greenrobot-common) is a set of utility classes and hash functions for Android & Java projects.

[Follow us on Google+](https://plus.google.com/b/114381455741141514652/+GreenrobotDe/posts) to stay up to date.


[1]: https://github.com/greenrobot/android-database-performance/blob/master/Common/build.gradle
[2]: https://github.com/greenrobot/android-database-performance/blob/master/Common/src/main/java/de/greenrobot/performance/BasePerfTestCase.java