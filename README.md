# ChatFilter
Simple Filter/Ban Plugin for Paper/Spigot/Bukkit

## Adding a Ban Message
* The Ban Message uses Bukkit Color Codes
* You can also define the date of the ban with `{DATE}`
* You can define the amount of time the player is banned with `{TIME}`
* Edit config.yml to something like this
```yaml
banMessage: |
    &cBeen Kicked Too Many Times by ChatFilter!
    &bBanned on &r{DATE} &bfor {TIME} hours
    &bTalk about your ban here: https://example.com
```

## Build
```bash
cd ChatFilter
mvn clean plackage
```
