# LensCast

<p align="center">
  <img src="docs/images/app-icon.png" alt="LensCast icon" width="128" height="128">
</p>

<p align="center">
  <strong>📷 Transforme seu celular Android em uma câmera de rede local capaz de utilizar todas as câmeras disponíveis — grande-angular, principal, teleobjetiva e outras.</strong>
</p>

<p align="center">
  <a href="#recurso">Recursos</a> ·
  <a href="#fluxos-de-vídeo">Fluxos de Vídeo</a> ·
  <a href="#como-usar">Como Usar</a> ·
  <a href="#compilação">Compilação</a> ·
  <a href="#aviso-de-segurança">Segurança</a> ·
  <a href="README.md">English</a> ·
  <a href="README.md">中文</a>
</p>

---

LensCast é um aplicativo Android que transforma seu celular em uma câmera IP de rede local. Seu grande diferencial não é simplesmente transformar o celular em uma IP Camera, mas sim acessar **todas as câmeras físicas expostas pela API Camera2**, incluindo grande-angular, principal, teleobjetiva/periscópica, câmera frontal e outras câmeras que seletores comuns de câmera normalmente ocultam.

> ⚠️ A disponibilidade real das lentes depende do grau de abertura do fabricante do celular. O LensCast inclui um fluxo de escaneamento para verificar quais câmeras podem ser abertas e transmitidas de forma estável por aplicativos de terceiros.

## Recursos

- 📷 **Acesso a múltiplas lentes**: grande-angular, principal, teleobjetiva, frontal e outras câmeras físicas expostas pela Camera2
- 🌐 **Painel web de controle**: controle o celular a partir de outro dispositivo na mesma rede local
- 🎥 **Preview MJPEG ao vivo**: útil para navegadores, incorporação web e alguns NVRs
- 📡 **Stream de vídeo RTSP**: compatível com Home Assistant, VLC, softwares NVR e outros clientes
- 🔊 **Stream de áudio AAC**: áudio do microfone opcional
- 🖼️ **Endpoint de snapshot**: salva o frame atual em JPEG no momento do clique
- ⚙️ **Controles remotos**: resolução, taxa de quadros, qualidade, zoom, foco, exposição e lanterna
- 🧭 **Sobreposição de vídeo**: data, hora, porcentagem de bateria e estado de carregamento com tamanho ajustável
- 🌙 **Modo paisagem e modo tela preta para economia de energia**: projetado para uso prolongado em câmera fixa

## Fluxos de Vídeo

| Tipo | Endereço | Uso | Status |
| --- | --- | --- | --- |
| Painel web | `http://<ip-do-celular>:41737/` | Controle e preview no navegador | ✅ Suportado |
| MJPEG | `http://<ip-do-celular>:41737/video.mjpeg` | Navegador / web / alguns NVRs | ✅ Suportado |
| Snapshot | `http://<ip-do-celular>:41737/snapshot.jpg` | Salvar frame JPEG atual | ✅ Suportado |
| Áudio AAC | `http://<ip-do-celular>:41737/audio.aac` | Stream de áudio independente | ✅ Suportado |
| RTSP | `rtsp://<ip-do-celular>:8554/live` | Clientes de vídeo H.264 | ✅ Suportado |
| WebRTC / ONVIF | - | Ainda não implementado | ⏳ Não suportado |

## Capturas de Tela

### Painel Web

![LensCast Web UI](docs/images/webui_EN.jpg)

### Escaneamento de Câmeras

![LensCast lens scan](docs/images/lenscan_EN.jpg)

## Como Usar

1. Instale o APK da página de releases no celular Android.
2. Conceda a permissão de câmera. Se precisar de áudio, conceda também a permissão de microfone.
3. Abra o LensCast e toque em **Escanear câmeras**.
4. Após o escaneamento, selecione a lente desejada na lista de câmeras verificadas.
5. Escolha um preset ou ajuste manualmente a resolução, taxa de quadros e qualidade de transmissão.
6. Toque em **Iniciar transmissão**.
7. Em outro dispositivo na mesma rede local, abra o endereço web exibido no aplicativo.

## Escaneamento de Câmeras

Os fabricantes Android expõem o hardware de múltiplas câmeras de formas diferentes. O LensCast escaneia os IDs de câmera da Camera2 e tenta identificar lentes que podem ser abertas e transmitidas de forma estável.

Execute um escaneamento na primeira utilização. O resultado verificado é armazenado em cache local e carregado nas próximas inicializações. Re-escaneie após trocar de celular, atualizar o sistema ou limpar dados do aplicativo.

Se uma lente aparece no escaneamento mas falha durante a transmissão ao vivo, isso geralmente indica que, embora o fabricante exponha o ID da câmera, ele ainda restringe o acesso de terceiros na prática.

## Compilação

Requisitos:

- Android Studio ou toolchain compatível com Android Gradle Plugin
- JDK 17
- Android SDK 34
- Gradle 8.7 ou versão compatível

Este snapshot do código-fonte não inclui arquivos locais do SDK nem o Gradle Wrapper. Compile com o Gradle local:

```bash
gradle assembleDebug
```

Nome do pacote Android atual:

```text
com.opencode.multilensipcam
```

## Aviso de Segurança

O LensCast é projetado para uso em rede local. **Não exponha** o painel web ou os endpoints de stream diretamente à internet pública sem adicionar autenticação própria, proxy reverso ou isolamento de rede.

## Tecnologias

| Categoria | Tecnologia |
| --- | --- |
| Linguagem | Kotlin |
| Min SDK | 25 (Android 7.1.1) |
| Target SDK | 34 (Android 14) |
| UI | ViewBinding + Material Components |
| Câmera | Camera2 API (lógica, física, alta velocidade) |
| Codificação de vídeo | MediaCodec H.264 + JPEG via `YuvImage` |
| Codificação de áudio | MediaCodec AAC-LC + AudioRecord |
| Rede | ServerSocket HTTP + ServerSocket RTSP (sem bibliotecas de terceiros) |
| Painel Web | HTML/CSS/JS embutido no código Kotlin |

## Licença

Apache License 2.0 — Copyright 2026 AlexTOOT