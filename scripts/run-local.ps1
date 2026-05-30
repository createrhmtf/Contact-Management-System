$securePassword = Read-Host "Enter the cms_app SQL Server password" -AsSecureString
$credential = [System.Net.NetworkCredential]::new("", $securePassword)
$env:DB_PASSWORD = $credential.Password

try {
    & "$PSScriptRoot\..\mvnw.cmd" spring-boot:run
}
finally {
    Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
}
