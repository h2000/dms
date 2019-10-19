java -jar "-Dvaadin.productionMode=true" ./dms.jar
if ($LastExitCode -eq 1337)
{
    write-host("Deleting old files...")
    Remove-Item lipsum.txt, options.default.properties, dms.jar, dms.zip

    [string]$sourceDirectory  = "update\*"
    [string]$destinationDirectory = ".\"
    $Exclude = @('run.ps1')
    write-host("Copying new files...")
    Copy-item -Force -Recurse -Verbose $sourceDirectory -Destination $destinationDirectory -Exclude $Exclude
    Remove-Item update -Recurse
    write-host("Starting updated dms...")
    java -jar "-Dvaadin.productionMode=true" ./dms.jar
}
pause