# Limpio

Este App es mi primer intento por crear un App con ayuda de IA Generativa (Claude). El App funciona bien, lo instalé en mi propio dispositivo. Como prueba considero que esta muy bien. Seguiré trabajando en apps creativas y nuevas, no como esta, que toma ideas de apps ya existentes. Espero que les sirva a todo aquel que quiere un app para limpiar su celular de manera fácil y rápida.

App de Android para limpiar tu galería deslizando fotos:

- **Izquierda** → marcar para eliminar
- **Derecha** → conservar
- Botones ✕ / ✓ como alternativa, y botón **deshacer**.
- Las fotos marcadas se eliminan **todas juntas** con un único aviso de confirmación del sistema
  (así no te aparece un diálogo por cada foto). Antes de confirmar puedes ver cuántas son y cuánto espacio liberas.

Requiere Android 11 o superior.

## Notas
- Al abrir la app te pedirá acceso a las fotos. En Android 14+ elige **"Permitir todo"** para ver toda la galería.
- El borrado es **permanente**. Si prefieres que vaya a la papelera del teléfono (recuperable ~30 días), en
  `MainActivity.kt` cambia `MediaStore.createDeleteRequest(contentResolver, uris)` por
  `MediaStore.createTrashRequest(contentResolver, uris, true)`.
- Si Google Fotos tiene respaldo activado, las copias en la nube no se tocan; elimínalas desde allí si lo deseas.
