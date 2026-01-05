package net.soundvibe.hasio;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JsonMapper;
import net.soundvibe.hasio.danfoss.protocol.DanfossDiscovery;
import net.soundvibe.hasio.danfoss.protocol.config.AppConfig;
import net.soundvibe.hasio.danfoss.protocol.config.DanfossBindingConfig;
import net.soundvibe.hasio.model.Options;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static net.soundvibe.hasio.Json.GSON;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static final Path DANFOSS_CONFIG_FILE = Paths.get("/share/danfoss-icon/danfoss_config.json");
    public static final Path DANFOSS_CONFIG_FILE_LOCAL = Paths.get("danfoss_config.json");
    public static final Path DANFOSS_CONFIG_DIR = Paths.get("/share/danfoss-icon");
    public static final Path ADDON_CONFIG_FILE = Paths.get("/data/options.json");
    public static final List<Path> CONFIG_FILES = List.of(DANFOSS_CONFIG_FILE, DANFOSS_CONFIG_FILE_LOCAL);
    public static void main(String[] args) {
        logger.info("starting danfoss icon addon...");
        var options = Options.fromPath(ADDON_CONFIG_FILE);
        logger.info("parsed options: {}", options.toString());
        changeRootLogLevel(options.logLevel());

        var app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.staticFiles.add("/public", Location.CLASSPATH);
            var gsonMapper = new JsonMapper() {
                @Override
                public @NotNull String toJsonString(@NotNull Object obj, @NotNull Type type) {
                    return GSON.toJson(obj, type);
                }

                @Override
                public <T> @NotNull T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                    return GSON.fromJson(json, targetType);
                }
            };
            config.jsonMapper(gsonMapper);
        });

        var bootstrapper = new Bootstrapper(app, options);

        app.get("/health", ctx -> ctx.result("OK"));
        app.post("/discover", ctx -> {
            var bindingConfig = DanfossBindingConfig.create(ctx.formParam("userName"));
            try (var discovery = new DanfossDiscovery(ctx.formParam("oneTimeCode"), bindingConfig)) {
                var response = discovery.discover();
                if (response.housePeerId != null) {
                    // persist
                    var appConfig = new AppConfig(bindingConfig.privateKey(), bindingConfig.userName(), response.housePeerId);
                    var appConfigJson = Json.toJsonString(appConfig);
                    ctx.html(STR."""
      <!DOCTYPE html>
      <html lang="en">
      <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Connection Successful</title>
          <link rel="stylesheet" href="/styles.css">
      </head>
      <body>
          <div class="container">
              <div class="header">
                  <h1>Danfoss Icon</h1>
                  <p>Connection established</p>
              </div>
              <div class="card">
                  <div class="message success">
                      Successfully discovered Icon house <strong>\{response.houseName}</strong>
                  </div>
                  <div class="config-header">
                      <h3>Configuration</h3>
                      <button class="copy-btn" onclick="copyConfig()">Copy</button>
                  </div>
                  <div class="config-output" id="configOutput">\{appConfigJson}</div>
                  <p style="margin-top: 16px; font-size: 13px; color: #666;">
                      If the addon doesn't start properly, write this configuration to<br>
                      <code style="background: #f5f5f5; padding: 2px 6px; border-radius: 4px;">/share/danfoss-icon/danfoss_config.json</code>
                  </p>
              </div>
          </div>
          <script>
              function copyConfig() {
                  const text = document.getElementById('configOutput').textContent;
                  navigator.clipboard.writeText(text).then(() => {
                      const btn = event.target;
                      btn.textContent = 'Copied!';
                      setTimeout(() => btn.textContent = 'Copy', 2000);
                  });
              }
          </script>
      </body>
      </html>
      """);
                    logger.info("discovered Icon house {} with {} peerId (privateKey: {}) successfully",
                            response.houseName, response.housePeerId, Arrays.toString(bindingConfig.privateKey()));
                    Files.createDirectories(DANFOSS_CONFIG_DIR);
                    Files.writeString(DANFOSS_CONFIG_FILE, appConfigJson);
                    discovery.close();
                    bootstrapper.load(appConfig);
                } else {
                    ctx.html(STR."""
      <!DOCTYPE html>
      <html lang="en">
      <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Connection Failed</title>
          <link rel="stylesheet" href="/styles.css">
      </head>
      <body>
          <div class="container">
              <div class="header">
                  <h1>Danfoss Icon</h1>
                  <p>Connection failed</p>
              </div>
              <div class="card">
                  <div class="message error">
                      House was not discovered. Please verify your credentials and try again.
                  </div>
                  <a href="/" style="text-decoration: none;">
                      <button type="button" class="btn">Try Again</button>
                  </a>
              </div>
          </div>
      </body>
      </html>
      """);
                }
            } catch (Throwable e) {
                logger.error("failed to discover a new house", e);
                ctx.html(STR."""
      <!DOCTYPE html>
      <html lang="en">
      <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Connection Error</title>
          <link rel="stylesheet" href="/styles.css">
      </head>
      <body>
          <div class="container">
              <div class="header">
                  <h1>Danfoss Icon</h1>
                  <p>Connection error</p>
              </div>
              <div class="card">
                  <div class="message error">
                      <strong>Error:</strong> \{e.getMessage()}
                  </div>
                  <p style="margin: 16px 0; font-size: 14px; color: #666;">
                      Please check your credentials and network connection, then try again.
                  </p>
                  <a href="/" style="text-decoration: none;">
                      <button type="button" class="btn">Try Again</button>
                  </a>
              </div>
          </div>
      </body>
      </html>
      """).status(500);
            }
        });

        app.start(options.port());
        logger.info("started addon on port {}", options.port());

        CONFIG_FILES.stream()
                .filter(Files::exists)
                .map(path -> Json.fromPath(path, AppConfig.class))
                .limit(1)
                .findAny()
                .ifPresentOrElse(appConfig -> {
                    logger.info("config file found, bootstrapping...");
                    bootstrapper.load(appConfig);
                }, () -> logger.info("config file not found, use ip:port/discover endpoint to discover new house"));
    }

    public static void changeRootLogLevel(String level){
        var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        var logger = loggerContext.getLogger("root");
        if (logger != null) {
            logger.setLevel(Level.toLevel(level));
        }
    }
}
