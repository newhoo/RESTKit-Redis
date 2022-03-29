# RESTKit-Redis

[简体中文](./README.zh_CN.md)

[RESTKit-Redis](#) is a plugin that support redis for [RESTKit](https://plugins.jetbrains.com/plugin/14723-restkit) plugin from version 2.0.3.


> Note  
> RESTKit plugin has provided extension point from version 2.0.0. You can also support your framework restful apis like this plugin.

If this plugin helps, please **🌟Star**! If you have any good idea, please let me know.


## Install
- **Using IDE plugin system**

Recommended <kbd>Preferences(Settings)</kbd> > <kbd>Plugins</kbd> > <kbd>Browse repositories...</kbd> > <kbd>find "RESTKit-Redis"</kbd> > <kbd>Install Plugin</kbd>

- **Local Install**

Download plugin form <kbd>distributions/RESTKit-Redis-x.x.x.zip</kbd>, then <kbd>Preferences(Settings)</kbd> > <kbd>Plugins</kbd> > <kbd>Install Plugin from Disk...</kbd>

## Usage
After installed this plugin, you should enable it in `RESTKit` setting. Refresh in `RESTKit` window.

![enable](./.images/setting.png)

Set redis address and project name for saving api.

![enable](./.images/address_config.png)


### Save api to redis

![enable](./.images/save_api_to_redis.png)

### Save redis command

![enable](./.images/save_redis_command.png)

### Send redis command

![enable](./.images/send_redis_command.png)


UI introduction:

- Config: request config for redis, can use environment variable. Content:
  - address: redis server address. The default is `{{redisAddress}}`. Without this pair or env variable, will use address in the config.
- Headers: useless, just ignore.
- Params: useless, just ignore.
- Body: redis command using string array format.
- Response：response content.
- Info：request and response info.