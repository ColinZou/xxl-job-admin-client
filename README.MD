# xxl-job-admin client

## About this client

xxl-job(https://github.com/xuxueli/xxl-job) is a distributed task scheduling framework.

And I did not find any client for scheduling jobs by calling xxl-job-admin api so far. Here it is.

## How to use this client

I just uploaded the code to oss snapshot: https://s01.oss.sonatype.org/content/repositories/snapshots/

### 1. add dependency (setup pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <!-- ... -->
    <repositories>
        <repository>
            <id>oss-sonatype</id>
            <name>oss sonatype</name>
            <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
            <releases>
                <enabled>false</enabled>
            </releases>
        </repository>
        <!--...-->
    </repositories>
    <dependencies>
        <dependency>
            <groupId>top.nb6</groupId>
            <artifactId>xxl-job-admin-client</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
        <!-- ... -->
    </dependencies>
</project>
```
### 2. calling api
Check the unittest code for details. src/main/test folder from xxl-job-admin-bio-client/xxl-job-admin-reactive-client.