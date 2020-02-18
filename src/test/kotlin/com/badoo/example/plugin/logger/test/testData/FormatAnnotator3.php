<?php

$Logger = new \Logger\Logger();

$Logger-><warning descr="Invalid format function usage">warningf</warning>("Hello <warning descr="Unused format item">%s</warning> world");
$Logger-><warning descr="Invalid format function usage">warningf</warning>("Hello <warning descr="Unused format item">%s</warning> <warning descr="Unused format item">%d</warning>");
$Logger-><warning descr="Invalid format function usage">warningf</warning>("Hello %s <warning descr="Unused format item">%d</warning>", "world");
