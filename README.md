# Demo Spring Boot Config Server
###### With refreshing client Appllication

### Config Server

The config server will serve a `yaml` or `properties` file to the application. I have set up the config server to be able to hld multiple application's profiles.

curl the URL `http://localhost:8888/{application-name}/{profiles}` and see what it returns.

Take a look at the application.yaml and bootstrap.yaml for configurations and modify as you'd like.

#### Notes:
 - *spring.cloud.config.server.git.searchPaths*: Use searchPaths for the way the config server will interpret its URL call. In this case I assume that my config server will host configs for more than one app. Therefore the searchPaths will be set to [{application}] indicating that we will search per app.
 - *spring.cloud.config.server.git.baseDir*: In the event that your application is fetching your config from a GIT repo (Recommended). Use this property to indicate where the repo should be cloned to.
 - *spring.cloud.config.server.git.uri*: This is the URI to your repo. You can have a local or remote repo to handle this.  Locally create a folder like '/shared/config/val-service/' and `git init` it, or place a 'https://github.com/whomever/whatever.git' for remote. This will be cloned to the 'baseDir' path.

Take a look at the [documentation](https://cloud.spring.io/spring-cloud-config/) and this [baeldung blog post](http://www.baeldung.com/spring-cloud-configuration)

#### Demo Set Up

I have set up a directory `/shared/config/config-repo/` and init a git repo for this demo:
```bash
mkdir -p /shared/config/config-repo/
cd /shared/config/config-repo/
git init
```

I then add a folder with my application name (for this demo its `val-demo-service`):
```bash
mkdir val-demo-service
cd val-demo-service
touch application.yaml
```

I edit the application.yaml and add a simple config:
```yaml
application:
    sample: This is my new string.
```

Then I commit
```bash
cd /shared/config/config-repo/
git add .
git commit -m "Init"
```

Now we can run the server application `mvn spring-boot:run -pl val-demo-config` and hit the URL `http://localhost:8888/val-demo-service/default`, this will return something like this (I used curl):
```bash
> curl http://localhost:8888/val-demo-service/default
# {"name":"val-demo-service","profiles":["default"],"label":null,"version":"88be013b25ebbdc69f07a3b88e3582d66673b119","state":null,"propertySources":[{"name":"/shared/config/config-repo/val-demo-service/application.yaml","source":{"application.sample":"This is my new string."}}]}
```

### Config Client Application

This application just starts up a embedded tomcat server and serves the value of the property `application.sampe` to its root REST endpoint. You should be able to access it on the URL `http://localhost:80800/`
Take a look at the application.yaml and bootstrap.yaml for configurations and modify as you'd like.

### Notes
##### bootstrap.yaml
- It is important that the application has a name, as this is how it tries to get the config. Remember that bootstrap config is loaded before the normal application config. This is so that you app has a name before trying to fetch config.
- *spring.cloud.config.uri*: Indicates where to fetch the config. Default is `http://localhost:8888/`
- *spring.cloud.config.failFast*: Set to `true` if you want the app to crash when it does not find the config server.
##### application.yaml
- *application.sample*: The string we want to see when we hit the `http://localhost:80800/` endpoint.
- *application.refreshDelay*: how often to check for a config change
- *application.refreshOnConfigChange*: Set `true` to refresh context, `false` to shutdown application when the config changes.
- *application.enableRefresh*: Set `true` to enable this feature, `false` to ignore.

#### Demo setup

Make sure that the server is not running and start the service application `mvn spring-boot:run -pl val-demo-config`. Note that the logs say, this is because we can't fetch our config:
```bash
...
2017-12-20 12:34:29.947  INFO 43792 --- [           main] c.c.c.ConfigServicePropertySourceLocator : Fetching config from server at: http://localhost:8888/
2017-12-20 12:34:30.125  WARN 43792 --- [           main] c.c.c.ConfigServicePropertySourceLocator : Could not locate PropertySource: I/O error on GET request for "http://localhost:8888/val-demo-service/default": Connection refused; nested exception is java.net.ConnectException: Connection refused
...
2017-12-20 12:34:33.028 ERROR 43792 --- [pool-1-thread-1] c.v.s.v.s.ConfigurationResetService      : Couldn't fetch configuration, make sure that the host [http://localhost:8888/] is available.
2017-12-20 12:34:34.133 ERROR 43792 --- [pool-1-thread-1] c.v.s.v.s.ConfigurationResetService      : Couldn't fetch configuration, make sure that the host [http://localhost:8888/] is available.
2017-12-20 12:34:35.237 ERROR 43792 --- [pool-1-thread-1] c.v.s.v.s.ConfigurationResetService      : Couldn't fetch configuration, make sure that the host [http://localhost:8888/] is available.
...
``` 

The application will not be able to fetch config, so hitting the `http://localhost:8080` endpoint will display the local config's string:
```bash
> curl http://localhost:8080
# If set up correctly I should never see this one :D
```

Now in another terminal I run the server with `mvn spring-boot:run -pl val-demo-config`, the client terminal should pick up the change and refresh:

```bash
...
2017-12-20 12:38:28.705  INFO 43847 --- [pool-1-thread-1] c.v.s.v.s.ConfigurationResetService      : Configuration was not up when the application started.
2017-12-20 12:38:28.705  INFO 43847 --- [pool-1-thread-1] c.v.s.v.s.ConfigurationResetService      : The application will refresh....
2017-12-20 12:38:28.706  INFO 43847 --- [      Thread-20] ationConfigEmbeddedWebApplicationContext : Closing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@ebaa6cb: startup date [Wed Dec 20 12:38:17 SAST 2017]; parent: org.springframework.context.annotation.AnnotationConfigApplicationContext@4a94ee4
2017-12-20 12:38:28.708  INFO 43847 --- [      Thread-20] o.s.c.support.DefaultLifecycleProcessor  : Stopping beans in phase 0
2017-12-20 12:38:28.709  INFO 43847 --- [      Thread-20] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans on shutdown
2017-12-20 12:38:28.709  INFO 43847 --- [      Thread-20] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans
2017-12-20 12:38:28.764  INFO 43847 --- [      Thread-20] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2017-12-20 12:38:28.816  INFO 43847 --- [      Thread-20] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@29aded56: startup date [Wed Dec 20 12:38:28 SAST 2017]; root of context hierarchy
2017-12-20 12:38:28.841  INFO 43847 --- [      Thread-20] trationDelegate$BeanPostProcessorChecker : Bean 'configurationPropertiesRebinderAutoConfiguration' of type [org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration$$EnhancerBySpringCGLIB$$a195c2da] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
...
```

Now that the application has refreshed, lets see what hitting `http://localhost:8080` endpoint gives us:
```bash
> curl http://localhost:8080
# This is my new string.
```

Now lets change the config in the repo and see the change. I modify the file `/shared/config/config-repo/val-demo-service/application.yaml`:
```yaml
application:
    sample: This is a changed property.
```

I commit these changes and watch the two terminals: 
```bash
cd /shared/config/config-repo/
git add .
git commit -m "updated"
```

The client should now be refreshed:
```bash
...
2017-12-20 12:52:52.209  INFO 44237 --- [pool-1-thread-1] c.v.s.v.s.ConfigurationResetService      : Configuration has changed to [{"name":"val-demo-service","profiles":["default"],"label":null,"version":"f824b17bea18b91aa4c94d46f4584e596aca6dda","state":null,"propertySources":[{"name":"/shared/config/config-repo/val-demo-service/application.yaml","source":{"application.sample":"This is a changed property."}}]}]
2017-12-20 12:52:52.210  INFO 44237 --- [pool-1-thread-1] c.v.s.v.s.ConfigurationResetService      : The application will refresh....
2017-12-20 12:52:52.211  INFO 44237 --- [      Thread-21] ationConfigEmbeddedWebApplicationContext : Closing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@ebaa6cb: startup date [Wed Dec 20 12:52:30 SAST 2017]; parent: org.springframework.context.annotation.AnnotationConfigApplicationContext@4a94ee4
2017-12-20 12:52:52.212  INFO 44237 --- [      Thread-21] o.s.c.support.DefaultLifecycleProcessor  : Stopping beans in phase 0
2017-12-20 12:52:52.213  INFO 44237 --- [      Thread-21] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans on shutdown
2017-12-20 12:52:52.213  INFO 44237 --- [      Thread-21] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans
2017-12-20 12:52:52.269  INFO 44237 --- [      Thread-21] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2017-12-20 12:52:52.319  INFO 44237 --- [      Thread-21] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@39350ef6: startup date [Wed Dec 20 12:52:52 SAST 2017]; root of context hierarchy
2017-12-20 12:52:52.343  INFO 44237 --- [      Thread-21] trationDelegate$BeanPostProcessorChecker : Bean 'configurationPropertiesRebinderAutoConfiguration' of type [org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration$$EnhancerBySpringCGLIB$$a195c2da] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
...
```
Now that the application has refreshed, lets see what hitting `http://localhost:8080` endpoint gives us:
```bash
> curl http://localhost:8080
# This is a changed property.
```
