 <?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "chatlog";

if(isset($_GET["code"])) {
	// Create connection
	$conn = new mysqli($servername, $username, $password, $dbname);
	
	// Check connection
	if ($conn->connect_error) {
		die("Connection failed: " . $conn->connect_error);
	}
	
	if (substr($_GET["code"], 0, 1) === "R") {
		$sql = "SELECT time, uuid, message FROM messages, restartcode WHERE restartcode.code = ? AND restartcode.restartnr = messages.restartnr AND restartcode.server = messages.server;";
	} else {
		$sql = "SELECT time, uuid, message FROM messages, timecode WHERE timecode.code = ? AND timecode.server = messages.server AND timecode.fromTime <= messages.time AND timecode.toTime >= messages.time;";
	}
	
	$stmt = $conn->stmt_init();
	
	if(!$stmt->prepare($sql)) {
		print "Failed to prepare statement\n";
	} else {
		$stmt->bind_param("s", $code);
		$code = $_GET["code"];
		
		$stmt->execute();
        $result = $stmt->get_result();
        while ($row = $result->fetch_array(MYSQLI_NUM)) {
            foreach ($row as $r)
            {
                print "$r ";
            }
            print "<br />";
        }
	}
	$conn->close();
} else {
	echo "Need Code!";
}
?> 