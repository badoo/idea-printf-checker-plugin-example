<?php

class Test {
    /** @var \Logger\Logger */
    private $Logger;

    function test() {
        $this->Logger-><warning descr="Invalid format function usage">warningf</warning>("Hello <warning descr="Unused format item">%s</warning> world");
        $this->Logger-><warning descr="Invalid format function usage">warningf</warning>("Hello <warning descr="Unused format item">%s</warning> <warning descr="Unused format item">%d</warning>");
        $this->Logger-><warning descr="Invalid format function usage">warningf</warning>("Hello %s <warning descr="Unused format item">%d</warning>", "world");
    }
}
