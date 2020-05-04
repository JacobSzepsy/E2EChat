<?php
    require("FuncDefs.php");
    errorReporting();
    $json = file_get_contents('php://input');
    $data = json_decode($json, TRUE);

    // echo(gettype($data));
    // if(isset($data["message"])){
    //     echo(json_encode($data["message"]));
    // }
    
    $type = $data["type"];
    $rtn = [];
    if($type == "login"){
        $user = $data["username"];
        $pass = $data["password"];
        $bool = authenticate($user, $pass);
        $rtn = ["auth" => $bool];
    }
    else if($type == "register"){
        $userid = $data["username"];
        $pass = $data["password"];
        $pubkey = $data["key"];
        $bool = insertNewUser($userid, $pass, $pubkey);
        $rtn = ["done" => $bool];
    }
    else if($type == "request"){
        $userid = $data["username"];
        $pass = $data["password"];
        $requestedUser = $data["requestedUser"];
        $key = getUserPubKey($userid, $pass, $requestedUser);
        $bool = TRUE;
        if($key == ""){
            $bool = FALSE;
        }
        $rtn = ["done" => $bool, "pubkey" => $key];
    }
    else if($type == "getMessage"){
        $userid = $data["username"];
        $pass = $data["password"];
        $requestedUser = $data["requestedUser"];
        if(authenticate($userid, $pass) && checkIfUserExists($requestedUser)){
            $rtn["done"] = TRUE;
            if(isset($data["timestamp"])){
                $x = getAllMessagesBetweenTwoUsersAfterTimeStamp($userid, $pass, $requestedUser, $data["timestamp"]);
                $rtn["messages"] = $x;
            }
            else{
                $x = getAllMessagesBetweenTwoUsers($userid, $pass, $requestedUser);
                $rtn["messages"] = $x;
            }
        }
        else{
            $rtn["done"] = FALSE;
        }
    }
    else if($type == "sendMessage"){
        $userid = $data["username"];
        $pass = $data["password"];
        $requestedUser = $data["requestedUser"];
        $dt = $data["timestamp"];
        // $message = utf8_encode($data["message"]);
        // $message = $data["message"];
        $message = json_encode($data["message"]);
        $bool = sendMessage($dt, $userid, $pass, $requestedUser, $message);
        $rtn = ["done" => $bool];
    }
    echo(json_encode($rtn));
?>