# Limpio

App de Android para limpiar tu galería deslizando fotos:

- **Izquierda** → marcar para eliminar
- **Derecha** → conservar
- Botones ✕ / ✓ como alternativa, y botón **deshacer**.
- Las fotos marcadas se eliminan **todas juntas** con un único aviso de confirmación del sistema
  (así no te aparece un diálogo por cada foto). Antes de confirmar puedes ver cuántas son y cuánto espacio liberas.

Requiere Android 11 o superior.

## Cómo obtener el APK

### Opción A — Sin computadora (GitHub)
1. Crea una cuenta gratis en github.com y un repositorio nuevo (por ejemplo `limpio`).
2. Sube **todo el contenido** de esta carpeta (incluida la carpeta oculta `.github`).
3. Ve a la pestaña **Actions** → **Compilar APK de Limpio** → **Run workflow**.
4. Espera unos 5 minutos. Al terminar, entra a la ejecución y descarga **Limpio-APK** (viene en un .zip con `app-debug.apk`).
5. Copia el APK al celular, ábrelo y permite "instalar apps de orígenes desconocidos" cuando te lo pida.

### Opción B — Android Studio
1. Instala Android Studio y abre esta carpeta (`File → Open`).
2. Espera a que sincronice Gradle.
3. `Build → Build Bundle(s) / APK(s) → Build APK(s)`.
4. El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Notas
- Al abrir la app te pedirá acceso a las fotos. En Android 14+ elige **"Permitir todo"** para ver toda la galería.
- El borrado es **permanente**. Si prefieres que vaya a la papelera del teléfono (recuperable ~30 días), en
  `MainActivity.kt` cambia `MediaStore.createDeleteRequest(contentResolver, uris)` por
  `MediaStore.createTrashRequest(contentResolver, uris, true)`.
- Si Google Fotos tiene respaldo activado, las copias en la nube no se tocan; elimínalas desde allí si lo deseas.
