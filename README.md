# Aternos Controller

App Android para controlar servidores Aternos de forma fácil e automática.

## Funcionalidades

- 🔐 Login integrado com Aternos.org
- ▶️ Ligar e parar servidor com um toque
- 👥 Visualizar quantos jogadores estão online
- 🎯 Gerenciar fila de entrada
- ✅ Aceitar fila automaticamente
- ⏰ Verificação automática a cada 3 minutos
- 🚀 Ligar servidor automaticamente quando offline
- 🎨 Interface moderna com Material Design 3 (Material You)

## Tecnologias

- Kotlin
- Jetpack Compose
- Material 3 (Material You)
- DataStore
- WorkManager
- WebView

## Build

Para compilar o projeto:

```bash
./gradlew assembleDebug
```

O APK será gerado em: `app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions

O projeto inclui um workflow do GitHub Actions que compila automaticamente o APK debug quando há push ou pull request na branch main/master.

## Configurações

- **Aceitar Fila Automaticamente**: Aceita automaticamente quando entrar na fila
- **Verificação Automática**: Verifica a cada 3 minutos se o servidor está ligado e liga automaticamente se necessário

## Licença

Este projeto é open source e está disponível sob a licença MIT.
