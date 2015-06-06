<?php
// This file automatically generates packages.json

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
        $document['packages'][] = [
            'name' => $data->name,
            'title' => isset($data->title) ? $data->title : $data->name,
            'version' => $data->version,
            'location' => basename($file),
            'priority' => 1,
        ];
    }
}

header("Content-Type: text/plain");
echo json_encode($document, JSON_PRETTY_PRINT);