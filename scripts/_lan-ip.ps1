# 같은 Wi-Fi 에서 접속할 때 쓸 이 PC 의 IPv4 주소 출력 (가상 네트워크·루프백 제외)
Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object { $_.IPAddress -notmatch '^(127\.|169\.254\.)' -and $_.InterfaceAlias -notmatch 'vEthernet|WSL|Loopback' } |
    ForEach-Object { $_.IPAddress }
