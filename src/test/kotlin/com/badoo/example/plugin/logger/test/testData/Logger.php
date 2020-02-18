<?php

namespace Logger;

class Logger {
    static function getLogger() {
        return new self();
    }
}