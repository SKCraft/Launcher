<?php
// This file automatically generates packages.json

$login = ['user' => 'pass']; //credentials

$files = glob("*.json");

if ($files === false) {
    $files = [];
}

$document = [
    'minimumVersion' => 1,
    'packages' => [],
];

foreach ($files as $file) {
    $data = json_decode(file_get_contents($file));
    if (isset($data->name) && isset($data->version)) {
        if(isset($_GET['user'], $_GET['user'])){
            $user = $_GET['user'];
            $mdp = $_GET['mdp'];
            if(isset($login[$user]) && $login[$user] == $mdp){
                $document['packages'][] = [
                    'name' => $data->name,
                    'title' => isset($data->title) ? $data->title : $data->name,
                    'version' => $data->version,
                    'location' => basename($file),
                    'priority' => 1,
                ];
            }
        }
    }
}

header("Content-Type: text/plain");
echo json_encode($document, JSON_PRETTY_PRINT);
