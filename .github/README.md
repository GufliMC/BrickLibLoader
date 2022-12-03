# BrickLibLoader

This plugin allow plugins to define different repositories to download libraries from.

## Install

Get the [release](https://github.com/GufliMC/BrickLeaderboards/releases) and place it in your server.

## Usage

As a plugin developer add the following to your plugin.yml.

```yaml
libraries:
  - group:name:version
repositories:
  name: 'https://url.to.repository'
```
