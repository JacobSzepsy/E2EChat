<?php
	require("account.php");
	date_default_timezone_set('America/New_York');
    function errorReporting()
    {
		error_reporting(E_ERROR | E_WARNING | E_PARSE | E_NOTICE);  
		ini_set('display_errors' , 1);
		return;
	}
	
	function mysqliOOP(){
		global $hostname, $username, $password, $project;
		$mysqli = new mysqli($hostname, $username, $password, $project);
		if (mysqli_connect_errno()) {
			printf("Connect failed: %s\n", mysqli_connect_error());
			exit();
		}
		return($mysqli);
	}

	function mysqliCloseOOP(&$ms){
		$ms->close();
	}

    function POST($fieldname, &$flag_empty, &$flag_isset)
    {
		// global $db;
		$flag_empty=TRUE;
		$flag_isset=FALSE;
		$flag_isset=isset($_POST[$fieldname]);
		if(!$flag_isset){
			//echo("smth");
			return;
		}
		$flag_isset=TRUE;
		$v=$_POST[$fieldname];
		$v=trim($v);
		if ($v==""){
			$flag_empty = true;
			//echo "<br><br>$fieldname is empty.";
			return;
		}
		// $v=mysqli_real_escape_string($db, $v);
		$flag_empty=FALSE;
		//echo "$fieldname is $v.<br>";
		//echo($v);
		return $v; 
	}
	
	function GET($fieldname, &$flag_empty, &$flag_isset){
		// global $db;
		$flag_empty=TRUE;
		$flag_isset=FALSE;
		$flag_isset=isset($_GET[$fieldname]);
		if(!$flag_isset){
			//echo("smth");
			return;
		}
		$flag_isset=TRUE;
		$v=$_GET[$fieldname];
		$v=trim($v);
		if ($v==""){
			$flag_empty = true;
			//echo "<br><br>$fieldname is empty.";
			return;
		}
		// $v=mysqli_real_escape_string($db, $v);
		$flag_empty=FALSE;
		//echo "$fieldname is $v.<br>";
		//echo($v);
		return $v; 
	}

	function getCurrentDate(){
		return(date("Y-m-d"));
	}

    function authenticate($user, $pass)
    {
		$ms = mysqliOOP();
		$query = "SELECT * FROM `USERS` WHERE `USERID` = '$user';";
		$res = $ms->query($query);
		if($res->num_rows <= 0){
			mysqliCloseOOP($ms);
			return(FALSE);
		}
		$row = $res->fetch_row();
		$hs = $row[1];
		if(!password_verify($pass, $hs)){
			mysqliCloseOOP($ms);
			return(FALSE);
		}
		mysqliCloseOOP($ms);
		return(TRUE);
	}

	function insertNewUser($userid, $pass, $pubkey){
		$ms = mysqliOOP();
		$hs = password_hash("$pass", PASSWORD_DEFAULT);
		$queryInsert = "INSERT INTO `USERS` (`USERID`, `PASSWORD`, `PUBKEY`) VALUES ('$userid', '$hs', '$pubkey');";
		if(!checkIfUserExists($userid)){
			$resI = $ms->query($queryInsert);
			if($resI){
				mysqliCloseOOP($ms);
				return(TRUE);
			}
			mysqliCloseOOP($ms);
			return(FALSE);
		}
		mysqliCloseOOP($ms);
		return(FALSE);
	}

	function getUserPubKey($userFROM, $passFROM, $userTO){
		if(!authenticate($userFROM, $passFROM)){
			return("");
		}
		$ms = mysqliOOP();
		$queryLookup = "SELECT * FROM `USERS` WHERE `USERID` = '$userTO';";
		$res = $ms->query($queryLookup);
		if($res->num_rows <= 0){
			mysqliCloseOOP($ms);
			return("");
		}
		$row = $res->fetch_row();
		$pubkey = $row[2];
		mysqliCloseOOP($ms);
		return($pubkey);
	}

	function checkIfUserExists($user){
		$ms = mysqliOOP();
		$queryLookup = "SELECT * FROM `USERS` WHERE `USERID` = '$user';";
		$resL = $ms->query($queryLookup);
		if($resL->num_rows > 0){
			mysqliCloseOOP($ms);
			return(TRUE);
		}
		mysqliCloseOOP($ms);
		return(FALSE);
	}

	function sendMessage($dt, $userFrom, $passwordFrom, $userTo, $message){
		if(authenticate($userFrom, $passwordFrom) && checkIfUserExists($userTo)){
			$query = "INSERT INTO `MESSAGES` (`MID`, `MDTIME`, `MUFR`, `MUTO`, `MESSAGE`) VALUES (NULL, '$dt', '$userFrom', '$userTo', '$message');";
			$ms = mysqliOOP();
			$resL = $ms->query($query);
			return(TRUE);
		}
		return(FALSE);
	}

	function messageMerge($row){ 
		$x = ["MID"=> $row["MID"], "MDTIME"=> $row["MDTIME"], "MUFR"=> $row["MUFR"], "MUTO"=> $row["MUTO"], "MESSAGE"=> $row["MESSAGE"]];
		$y = "" . $row["MDTIME"] . "" . $row["MUFR"] . ": " . $row["MESSAGE"];
		$z = [[$row["MDTIME"]], [$row["MUFR"]], [json_decode($row["MESSAGE"], TRUE)]];
		return($z);
	}

	function getAllMessagesBetweenTwoUsers($user1, $pass1, $user2){
		$rtn = [];
		if(authenticate($user1, $pass1) && checkIfUserExists($user2)){
			// $query = "SELECT * FROM (SELECT * FROM `MESSAGES` WHERE `MUFR` = '$user1' AND `MUTO` = '$user2' UNION SELECT * FROM `MESSAGES` WHERE `MUFR` = '$user2' AND `MUTO` = '$user1') AS `A` ORDER BY `A`.`MID` ASC;";
			$query = "SELECT * FROM (SELECT * FROM `MESSAGES` WHERE `MUFR` = '$user2' AND `MUTO` = '$user1') AS `A` ORDER BY `A`.`MID` ASC;";			
			$ms = mysqliOOP();
			$res = $ms->query($query);
			if($res->num_rows > 0){
				while($row = $res->fetch_assoc()){
					$rtn[] = messageMerge($row);
				}
			}
			mysqliCloseOOP($ms);
		}
		return($rtn);
	}

	function getAllMessages(){
		$rtn = [];
		$ms = mysqliOOP();
		$query = "SELECT * FROM `MESSAGES`;";
		$res = $ms->query($query);
		mysqliCloseOOP($ms);
		if($res->num_rows > 0){
			while($row = $res->fetch_assoc()){
				$rtn[] = messageMerge($row);
			}
		}
		return($rtn);
	}

	function truncateMessages(){
		$query = "TRUNCATE TABLE `MESSAGES`;";
		$ms = mysqliOOP();
		$res = $ms->query($query);
		mysqliCloseOOP($ms);
	}

	function truncateUsers(){
		$query = "TRUNCATE TABLE `USERS`;";
		$ms = mysqliOOP();
		$res = $ms->query($query);
		mysqliCloseOOP($ms);
	}

	function getAllMessagesBetweenTwoUsersAfterTimeStamp($user1, $pass1, $user2, $dt){
		$rtn = [];
		if(authenticate($user1, $pass1) && checkIfUserExists($user2)){
			// $query = "SELECT * FROM (SELECT * FROM `MESSAGES` WHERE `MUFR` = '$user1' AND `MUTO` = '$user2' UNION SELECT * FROM `MESSAGES` WHERE `MUFR` = '$user2' AND `MUTO` = '$user1') AS `A` WHERE `A`.`MDTIME` BETWEEN CAST('$dt'AS DATETIME) AND NOW() ORDER BY `A`.`MID` ASC;";
			$query = "SELECT * FROM (SELECT * FROM `MESSAGES` WHERE `MUFR` = '$user2' AND `MUTO` = '$user1') AS `A` WHERE `A`.`MDTIME` BETWEEN CAST('$dt'AS DATETIME) AND NOW() ORDER BY `A`.`MID` ASC;";
			$ms = mysqliOOP();
			$res = $ms->query($query);
			if($res->num_rows > 0){
				while($row = $res->fetch_assoc()){
					$rtn[] = messageMerge($row);
				}
			}
			mysqliCloseOOP($ms);
		}
		return($rtn);
	}
?>