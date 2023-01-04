package ru.aasmc.xpensemanager.domain.exceptions

import java.io.IOException

class NetworkUnavailableException(message: String = "No network available :(") : IOException(message)
