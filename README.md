# Hackit Template

To run:

```
sbt

> project backend
> reStart
```

Navigate to [http://localhost:8080/](http://localhost:8080/).

You can build a fully self-contained jar using `assembly` in the backend project.

## Configuration

You can set `app.interface` and `app.port` in `application.conf` to configure where the server
should listen to.

This also works on the command line using JVM properties, e.g. using `reStart`:

```
> reStart --- -Dapp.interface=0.0.0.0 -Dapp.port=8080
```

will start the server listening on all interfaces.
