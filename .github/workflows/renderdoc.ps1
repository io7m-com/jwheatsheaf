$uri = 'https://renderdoc.org/stable/1.41/RenderDoc_1.41_64.msi'
Invoke-WebRequest -Uri $uri -OutFile renderdoc.msi -UseBasicParsing
Start-Process msiexec.exe -Wait -ArgumentList '/i', (Resolve-Path renderdoc.msi), '/qn', '/norestart'
& 'C:\Program Files\RenderDoc\qrenderdoc.exe' --version
