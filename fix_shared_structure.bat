@echo off
if not exist E:\Houzicore\Code\Shared\src\main\java\com\houzicore\shared\common\util mkdir E:\Houzicore\Code\Shared\src\main\java\com\houzicore\shared\common\util
if not exist E:\Houzicore\Code\Shared\src\main\java\com\houzicore\shared\command mkdir E:\Houzicore\Code\Shared\src\main\java\com\houzicore\shared\command
move E:\Houzicore\Code\Shared\src\main\java\com\houzicore\shared\common\core\common\util\* E:\Houzicore\Code\Shared\src\main\java\com\houzicore\shared\common\util\
move E:\Houzicore\Code\Shared\src\main\java\com\houzicore\shared\mineplex\core\command\* E:\Houzicore\Code\Shared\src\main\java\com\houzicore\shared\command\
