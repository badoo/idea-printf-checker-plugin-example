<?php

$Logger = new \Logger\Logger();

$Logger-><warning descr="Invalid format function usage">infof</warning>("Hello %s", "world", <warning descr="Format line expecting only 1 parameters">"test"</warning>);
$Logger-><warning descr="Invalid format function usage">warningf</warning>("Hello world", <warning descr="No format item found in first parameter but call has more than one argument">"test"</warning>);
$Logger-><warning descr="Invalid format function usage">errorf</warning>("%s %s",
    "Hello",
    "world",
    <warning descr="Format line expecting only 2 parameters">"test"</warning>,
    <warning descr="Format line expecting only 2 parameters">"test2"</warning>
);
