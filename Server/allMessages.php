<?php
    require("FuncDefs.php");
    errorReporting();
    echo(json_encode(getAllMessages()));
?>