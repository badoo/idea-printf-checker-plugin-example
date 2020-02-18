<?php
require 'Logger.php';

$Logger = new \Logger\Logger();

$Logger->infof("Hello world");
$Logger->debugf("Hello %s", "world");
