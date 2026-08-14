package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.EgressoEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertNotNull(appName)
  }

  @Test
  fun `egresso entity creation with SGDE`() {
    val egresso = EgressoEntity(
      nome = "Maria Oliveira",
      codigo = "SGDE-01-04",
      caixaArquivo = "Caixa 01",
      pastaProtocolo = "Pasta 04",
      statusDocumento = "2ª via digital",
      formatoEnvioDigital = "WhatsApp",
      dataEnvioDigital = "12/08/2026"
    )
    assertEquals("SGDE-01-04", egresso.codigo)
    assertEquals("2ª via digital", egresso.statusDocumento)
    assertEquals("WhatsApp", egresso.formatoEnvioDigital)
  }
}
