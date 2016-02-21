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

Each turn pLayers can build on plain ground settlements:
- villages which brings new resources (build cost 5 wood)
- TODO: forts which brings new territory to build (build cost 5 stones)
- or skip turn (TODO)

Once a time village bring resources from surrounding tiles: wood (forest), stone (mountain), food (sea)

Each fort consumes food once a time

Fort open territory to expand (2 hexes each direction).

TODO: Territory which is under control of 2+ enemy forts cannot bring resources.

...