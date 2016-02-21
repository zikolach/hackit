# Hackit Template

## TL;DR

```
git clone https://github.com/zikolach/hackit.git && \
cd hackit && \
git checkout global-hackathon-2016 && \
sbt "; project backend; ~reStart"                 
```

## Getting started

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


## Game rules

Landscape contains:
- plains
- mountains
- forests
- seas

PLayers can build on plain ground settlements:
- villages which brings new resources
- forts which brings new territory to build

Once a time village bring random resource from surrounding tiles: wood (forest), stone (mountain), food (plain or sea)

Each fort consumes food once a time


