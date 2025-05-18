# Calculator Pro
A text-based calculator featuring both panel and in-game chat functionality through the !calc command. 
Supports decimals, positive and negative numbers, math functions (sqrt, sin, cos, tan, ^2), brackets, custom tags and functions, and some commands.

## Features
### Custom Tags
Define tags with a desired value to use in your calculations
![Custom_Tags](https://imgur.com/X6Lc36R)

### Pre-Set Tags
Several useful pre-set tags allow for quick access use.
- Current xp lookup supports all skills `!calc myherb`
- Level xp lookup supports all levels from level 1-126 `!calc lvl95`
- Use any combination of these in your calculations `!calc (lvl99-myherb)/87.5`
- Quickly use the result of your last calculation `!calc 3*last`

### Custom Functions
Define a function with variables to calculate during runtime
![Custom_Functions](https://imgur.com/lZf0EFP)

### Complex Calculations
Handles complex calculations, including nested functions, scientific notation, and follows standard BEDMAS rules
![Complex_Calculations](https://imgur.com/3MjeC1N)

### Custom Equation
via the side panel, a custom equation calculation will be performed when calling `!calc` with nothing following it. Useful for complex
or repeated calculations
![Custom_Equation](https://imgur.com/Kv8QWno)

### Side Panel Calculator UI
Supports decimals, positive and negative numbers, and brackets
![Panel_Calculator](https://imgur.com/E6dHKyn)

### Commands
- `!calc !clear` Clears all current RunTime Tags
- `!calc !remove TagName` Removes a single desired tag from RunTime Tags
- `!calc !list RunTimeTags` Lists all current RunTime tags
- `!calc !list CustomTags` Lists all current CustomTags

## Update Log
### May 2025
- Custom Functions
- RunTime & CustomTag list viewing `!calc !list CustomTags`
### Apr 2025
- Current xp Lookup   `!calc myslay`
- Config Panel Updated

### Dec 2023
- Scientific Notation
- lvl100 to lvl126 pre-sets added

### Apr 2023
- Plugin Created