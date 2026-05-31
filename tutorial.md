# LensCast: Do Fork ao Controle Remoto via Termux

Como transformei um fork do LensCast em uma câmera IP controlável por linha de comando — e o que aprendi no caminho.

---

## O que é o LensCast

O LensCast é um app Android que transforma seu celular em uma câmera IP de rede local. A diferença para outros apps do gênero é que ele acessa **todas as câmeras** expostas pela API Camera2 — grande-angular, principal, teleobjetiva, frontal — inclusive lentes que apps comuns ignoram.

Ele serve um painel web em `http://<ip-do-celular>:41737/` de onde você controla resolução, FPS, qualidade, zoom, foco, exposição e lanterna, tudo remotamente pelo navegador.

<!-- screenshot: painel web do LensCast -->
![Painel web do LensCast](docs/images/webui_EN.jpg)

## Por que fazer um fork

O projeto original ([AlexTOOT/LensCast](https://github.com/AlexTOOT/LensCast)) funciona bem, mas eu precisava de duas coisas que não existiam:

1. **Controle remoto via linha de comando** — quero iniciar e parar a transmissão pelo Termux via SSH, sem precisar tocar na tela.
2. **Documentação em português** — para facilitar o uso e a contribuição de quem fala pt-br.

## O que foi adicionado

### 1. Controle via Intent e BroadcastReceiver

Criei um `BroadcastReceiver` (`StreamControlReceiver`) e actions de Intent na `MainActivity` que permitem controlar a transmissão a partir do Termux ou ADB:

```bash
# Iniciar transmissão
am start -n com.opencode.multilensipcam/.MainActivity -a com.opencode.multilensipcam.START

# Parar transmissão
am start -n com.opencode.multilensipcam/.MainActivity -a com.opencode.multilensipcam.STOP

# Alternar (liga/desliga)
am start -n com.opencode.multilensipcam/.MainActivity -a com.opencode.multilensipcam.TOGGLE

# Via broadcast (também funciona)
am broadcast -a com.opencode.multilensipcam.START
am broadcast -a com.opencode.multilensipcam.STOP
am broadcast -a com.opencode.multilensipcam.TOGGLE
```

**Arquivos criados/modificados:**

| Arquivo | Mudança |
|---|---|
| `StreamControlReceiver.kt` | Novo — BroadcastReceiver que redireciona para a MainActivity |
| `MainActivity.kt` | Adicionado `onNewIntent()` + `handleStreamIntent()` |
| `AndroidManifest.xml` | `launchMode="singleTop"`, intent-filters, declaração do receiver |

<!-- screenshot: Termux rodando os comandos am start/broadcast -->
<!--
![Controle via Termux](docs/images/termux-control.jpg)
-->

### 2. Endpoints HTTP de controle

Adicionei três endpoints REST ao servidor HTTP embutido do LensCast:

| Endpoint | Ação |
|---|---|
| `GET /api/start` | Inicia a transmissão |
| `GET /api/stop` | Para a transmissão |
| `GET /api/toggle` | Alterna (liga se desligado, desliga se ligado) |

Todos retornam `{"ok": true}` e podem ser chamados com `curl`:

```bash
curl http://192.168.15.185:41737/api/start
curl http://192.168.15.185:41737/api/stop
curl http://192.168.15.185:41737/api/toggle
```

**Arquivos modificados:**

| Arquivo | Mudança |
|---|---|
| `WebHttpRoutes.kt` | Novas rotas `STREAM_START`, `STREAM_STOP`, `STREAM_TOGGLE` |
| `MjpegHttpServer.kt` | Roteamento dos novos endpoints para `controlResponses.handleControl()` |

### 3. Botões de controle no painel web

O painel já tinha um botão toggle, mas agora também inclui botões dedicados Start/Stop/Toggle que chamam a API via `fetch()` sem sair da página:

<!-- screenshot: painel web com os novos botões de API -->
<!--
![Botões de API no painel](docs/images/webui-api-buttons.jpg)
-->

**Arquivo modificado:**

| Arquivo | Mudança |
|---|---|
| `WebDashboardPage.kt` | HTML, CSS e JS dos botões de API |

### 4. Preset de economia de recursos

Adicionei um preset **480p 15fps 40%** para uso prolongado via Termux/SSH com consumo mínimo:

```text
4K 30 fps 60%    — máxima qualidade
1080p 30 fps 75% — bom equilíbrio
720p 30 fps 100% — qualidade alta
480p 15 fps 40%  — economia de recursos ← novo
```

<!-- screenshot: preset 480p15 selecionado no painel web -->
<!--
![Preset 480p15](docs/images/preset-480p15.jpg)
-->

### 5. Documentação em português

Criei o `README.pt-br.md` com tradução completa do README original eadicionei referências cruzadas entre os idiomas.

## Lições aprendidas (erros que cometi)

### Erro 1: Links `<a href>` para ações de estado

A primeira implementação dos botões de API usou `<a href="/api/start">`. Isso **navega para fora do dashboard** — o usuário clicava e era levado a uma página JSON em branco.

**Correção:** Trocar por `<button>` + `addEventListener('click')` + `fetch()`, que chama a API sem sair da página:

```javascript
document.getElementById('apiStartButton').addEventListener('click', async () => {
  await startStreaming();  // fetch('/api/control?streaming=true')
});
```

### Erro 2: Broadcast não liga câmera em segundo plano

O `BroadcastReceiver` funciona, mas o Android **não permite acesso à câmera com a Activity em segundo plano**. O broadcast abre a Activity com `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_SINGLE_TOP`, que por sua vez chama `handleStreamIntent()`. Se a Activity estiver parada (`mStopped=true`), a câmera não abre.

**Solução prática:** Combine `am start` com `input keyevent` para manter a tela desperta, ou use o controle HTTP via `curl` com o app em foreground:

```bash
# 1. Garanta que o app está em foreground
am start -n com.opencode.multilensipcam/.MainActivity

# 2. Controle via curl (funciona remotamente pelo Termux/SSH)
curl http://192.168.15.185:41737/api/start
curl http://192.168.15.185:41737/api/stop
```

### Erro 3: SDK em /tmp

Configurei o Android SDK em `/tmp/opencode/android-sdk`, que foi limpo entre sessões. Isso quebrou o build na segunda tentativa.

**Correção:** Instalar o SDK em `~/android-sdk` e apontar `local.properties` para lá:

```properties
sdk.dir=/home/alexand7e/android-sdk
```

### Erro 4: `onNewIntent` e `singleTop`

Adicionar intent-filters sem `launchMode="singleTop"` faria o Android criar múltiplas instâncias da Activity ao receber comandos consecutivos. Sempre defina `singleTop` quando usar `onNewIntent()`.

## Como compilar

Requisitos: JDK 17, Android SDK 34 (platforms + build-tools), Gradle 8.7+.

```bash
# Gerar o wrapper (se necessário)
gradle wrapper --gradle-version 8.7

# Compilar o APK de debug
./gradlew assembleDebug

# O APK sai em:
# app/build/outputs/apk/debug/app-debug.apk
# LensCast-debug-latest.apk (na raiz do projeto)
```

## Como instalar e usar

```bash
# Instalar via ADB
adb install -r LensCast-debug-latest.apk

# Conceder permissões via ADB
adb shell pm grant com.opencode.multilensipcam android.permission.CAMERA
adb shell pm grant com.opencode.multilensipcam android.permission.RECORD_AUDIO

# Abrir o app
adb shell am start -n com.opencode.multilensipcam/.MainActivity

# Controlar via curl (com app em foreground)
IP=$(adb shell ip addr show wlan0 | grep 'inet ' | awk '{print $2}' | cut -d/ -f1)

curl http://$IP:41737/api/start    # iniciar
curl http://$IP:41737/api/stop     # parar
curl http://$IP:41737/api/toggle   # alternar
curl http://$IP:41737/api/state    # estado atual

# Controlar via Intent
adb shell am start -n com.opencode.multilensipcam/.MainActivity -a com.opencode.multilensipcam.START
adb shell am start -n com.opencode.multilensipcam/.MainActivity -a com.opencode.multilensipcam.STOP
adb shell am start -n com.opencode.multilensipcam/.MainActivity -a com.opencode.multilensipcam.TOGGLE
```

## Automatizando com Termux via SSH

Com o app aberto no celular e o Termux rodando, você pode automatizar por SSH:

```bash
# script: start_stream.sh
IP="192.168.15.185"
curl -s "http://$IP:41737/api/start" > /dev/null
echo "Stream iniciado em rtsp://$IP:8554/live"
```

```bash
# script: stop_stream.sh
IP="192.168.15.185"
curl -s "http://$IP:41737/api/stop" > /dev/null
echo "Stream parado"
```

```bash
# script: check_stream.sh
IP="192.168.15.185"
curl -s "http://$IP:41737/api/state" | python3 -c "import sys,json; d=json.load(sys.stdin); print('streaming:', d['streaming'])"
```

## Endpoint de estado

O endpoint `/api/state` retorna JSON completo com todos os parâmetros:

```bash
curl -s http://192.168.15.185:41737/api/state | python3 -m json.tool
```

Campos úteis para automação:

| Campo | Tipo | Descrição |
|---|---|---|
| `streaming` | boolean | Se a transmissão está ativa |
| `selectedCameraKey` | string | Câmera selecionada |
| `selectedResolution` | string | Resolução atual (ex: `1920x1080`) |
| `selectedFps` | int | FPS atual |
| `qualityValue` | int | Qualidade JPEG (30-100) |
| `audioEnabled` | boolean | Se áudio está habilitado |

<!-- screenshot: saída do /api/state no terminal -->
<!--
![API state output](docs/images/api-state-output.jpg)
-->

## Fluxos de vídeo disponíveis

| Tipo | Endereço | Uso |
|---|---|---|
| Painel web | `http://<ip>:41737/` | Controle e preview |
| MJPEG | `http://<ip>:41737/video.mjpeg` | Browser / NVR |
| Snapshot | `http://<ip>:41737/snapshot.jpg` | Frame atual em JPEG |
| Áudio AAC | `http://<ip>:41737/audio.aac` | Stream de áudio |
| RTSP | `rtsp://<ip>:8554/live` | VLC / Home Assistant / NVR |

## Arquitetura das contribuições

```
┌─────────────┐    ┌──────────────────────┐    ┌──────────────┐
│  Termux/SSH │───>│  curl /api/start     │───>│              │
│  (curl)     │    │  curl /api/stop      │    │              │
└─────────────┘    └──────────────────────┘    │              │
                                                │              │
┌─────────────┐    ┌──────────────────────┐    │  MjpegHttp   │
│  ADB/Termux │───>│  am broadcast        │───>│  Server      │
│  (intent)   │    │  (START/STOP/TOGGLE) │    │  (port 41737)│
└─────────────┘    └──────────────────────┘    │              │
                                                │              │
┌─────────────┐    ┌──────────────────────┐    │              │
│  Navegador  │───>│  Botões Start/Stop/  │───>│              │
│  (web UI)   │    │  Toggle no dashboard │    │              │
└─────────────┘    └──────────────────────┘    └──────┬───────┘
                                                       │
                                              ┌──────▼───────┐
                                              │  Camera2 API │
                                              │  + MediaCodec│
                                              └──────────────┘
```

## Commits

| Hash | Descrição |
|---|---|
| `8720866` | README em pt-BR |
| `8056f3e` | Controle via Intent e BroadcastReceiver (ADB/Termux) |
| `e884b19` | Endpoints `/api/start`, `/api/stop`, `/api/toggle` + preset 480p15 |
| `8d04f3f` | Fix: botões de API usam `fetch()` em vez de navegação |

---

_*Este post documenta contribuições feitas ao fork [alexand7e/LensCast](https://github.com/alexand7e/LensCast), baseado no projeto original de [AlexTOOT](https://github.com/AlexTOOT/LensCast)._