$files = Get-ChildItem -Path "src" -Recurse -Filter "*.java"
foreach ($file in $files) {
    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $newBytes = $bytes[3..($bytes.Length - 1)]
        [System.IO.File]::WriteAllBytes($file.FullName, $newBytes)
        Write-Host "Fixed: $($file.FullName)"
    }
}
Write-Host "Done!"
