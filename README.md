![Spring Boot & Cucumber Example](img/logo.png)

An example project to teach myself Spring Boot & Spring MVC
(by creating an actual multi-tenant application with a Cassandra backend)
and show how it can be integrated with Ruby Cucumber for integration testing using BDD:

https://cucumber.io/

# Setup

## Ruby

Cucumber is a Ruby-based tool, hence it needs to be installed first.

### Linux

Use distro package manager

#### Fedora

    sudo dnf install ruby ruby-devel
    
### Ubuntu

    sudo apt-get install ruby ruby-dev

### Windows

Use RubyInstaller: https://rubyinstaller.org/


## Cucumber

Use the standard Ruby **bundler** tool:

    bundle install
    
That will download and install all the dependencies (as defined in the *Gemfile*)

# Running BDD integration tests

## Cassandra

This application requires Cassandra to be running locally with the standard CQL ports.

### Optimizing for faster BDD test runs

To make the BDDs run faster, we recommend pointing it to **ramdisk** on Linux,
via these settings in **cassandra.yaml**

    data_file_directories:
         - /dev/shm/cassandra/data/data

    commitlog_directory: /dev/shm/cassandra/data/commitlog

    saved_caches_directory: /dev/shm/cassandra/data/saved_caches
    
    auto_snapshot: false

This will make data truncation really fast, as all Cassandra data files will be in RAM
and never hit actual disk

## Gradle task

The Gradle task

    ./gradlew bdd
    
will:

* build the Spring Boot executable JAR
* run it in the background (it will connect to Cassandra)
* run Cucumber BDDs (in *src/test/cucumber*)
* shut down Spring Boot JAR after BDDs are finished

BDD test reports in JUnit XML format can be found in

    build/test-results/junit
    
so it's easy to integrate this into a CI tool like Jenkins