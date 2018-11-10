package io.raspberrywallet.server

import io.ktor.html.HtmlContent
import kotlinx.html.*


val indexPage = HtmlContent {
    head {
        title { +"Raspberry Wallet" }
        link(rel = "Stylesheet", type = "text/css", href = "/style.css")
    }
    body {
        h1 { a(href = "/index.html") { +"Webapp" } }
        h2 { +"Utils" }
        ul {
            li {
                a(href = Paths.Utils.ping) { +Paths.Utils.ping }
            }
            li {
                a(href = Paths.Utils.cpuTemp) { +Paths.Utils.cpuTemp }
            }
        }
        h2 { +"Network" }
        ul {
            li {
                a(href = Paths.Network.wifiStatus) { +Paths.Network.wifiStatus }
            }
            li {
                a(href = Paths.Network.networks) { +Paths.Network.networks }
            }
            li {
                a(href = Paths.Network.setWifi) { +Paths.Network.setWifi }
            }
            li {
                a(href = Paths.Network.setupWiFi) { +Paths.Network.setupWiFi }
            }
        }
        h2 { +"Modules" }
        ul {

            li {
                a(href = Paths.Modules.modules) { +Paths.Modules.modules }
            }
            li {
                a(href = Paths.Modules.moduleState) { +Paths.Modules.moduleState }
            }
            li {
                a(href = Paths.Modules.nextStep) { +Paths.Modules.nextStep }
            }
            li {
                a(href = Paths.Modules.restoreFromBackupPhrase) { +Paths.Modules.restoreFromBackupPhrase }
            }
            li {
                a(href = Paths.Modules.walletStatus) { +Paths.Modules.walletStatus }
            }
            li {
                a(href = Paths.Modules.unlockWallet) { +Paths.Modules.unlockWallet }
            }
            li {
                a(href = Paths.Modules.lockWallet) { +Paths.Modules.lockWallet }
            }
        }
        h2 { +"Bitcoin" }
        ul {
            li {
                a(href = Paths.Bitcoin.currentAddress) { +Paths.Bitcoin.currentAddress }
            }
            li {
                a(href = Paths.Bitcoin.freshAddress) { +Paths.Bitcoin.freshAddress }
            }
            li {
                a(href = Paths.Bitcoin.estimatedBalance) { +Paths.Bitcoin.estimatedBalance }
            }
            li {
                a(href = Paths.Bitcoin.availableBalance) { +Paths.Bitcoin.availableBalance }
            }
        }
    }
}

val setNetwork = HtmlContent {
    head {
        title { +"Change Wi-Fi settings" }
        link(rel = "Stylesheet", type = "text/css", href = "/style.css")
        script { src = "/scripts.js"; type = "text/javascript" }
        script { src = "/jquery.min.js"; type = "text/javascript" }
    }
    body {
        h1 { a(href = "/index/") { +"<- Back" } }
        h2 { +"New Wi-Fi config" }
        h3 { +"ESSID:" }
        form(method = FormMethod.post, action = Paths.Network.setWifi) {
            select {
                id = "ssid"
                name = "ssid"
                for (network in manager.networkList) {
                    option {
                        value = network
                        +network
                    }
                }
            }
            span {
                onClick = "refreshNetworks()"
                style = "cursor: pointer, link, hand"
                +"Refresh"
            }
            h3 { +"Pre shared key:" }
            input(type = InputType.password, name = "psk") {}
            input(type = InputType.submit) {}
        }
    }
}

val status = HtmlContent {
    head {
        title { +"System status" }
        link(rel = "Stylesheet", type = "text/css", href = "/style.css")
    }
    body {
        h1 { a(href = "/index/") { +"<- Back" } }
        h2 { +"System status" }
        div(classes = "temperature") {
            +"Temperature: "
            when {
                manager.cpuTemperature.toFloat() > 47 -> span(classes = "hot") { +(manager.cpuTemperature + " 'C") }
                manager.cpuTemperature.toFloat() < 40 -> span(classes = "cold") { +(manager.cpuTemperature + " 'C") }
                else -> span(classes = "medium") { +(manager.cpuTemperature + " 'C") }
            }
        }
        a(href = Paths.Network.setupWiFi) {
            +"Configure Wi-Fi"
        }
        table {
            for ((param, value) in manager.wifiStatus) {
                tr {
                    td(classes = "param") { +param }
                    td { +value }
                }
            }
            for ((param, value) in manager.wifiConfig) {
                tr {
                    td(classes = "param") { +param }
                    td { +value }
                }
            }
        }
    }
}
