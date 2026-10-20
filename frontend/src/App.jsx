import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { RutaPaso, RutaPublica, RutaRol } from './componentes/Rutas'
import { SesionProvider, useSesion } from './contexto/SesionContext'
import ConsolaAdmin from './paginas/admin/ConsolaAdmin'
import CambiarClave from './paginas/acceso/CambiarClave'
import Ingresar from './paginas/acceso/Ingresar'
import Recuperar from './paginas/acceso/Recuperar'
import Registro from './paginas/acceso/Registro'
import Restablecer from './paginas/acceso/Restablecer'
import Verificacion from './paginas/acceso/Verificacion'
import VincularAutenticador from './paginas/acceso/VincularAutenticador'
import CatalogoTramites from './paginas/ciudadano/CatalogoTramites'
import DetalleIncidencia from './paginas/ciudadano/DetalleIncidencia'
import DetalleSolicitud from './paginas/ciudadano/DetalleSolicitud'
import MisIncidencias from './paginas/ciudadano/MisIncidencias'
import PasarelaPagos from './paginas/ciudadano/PasarelaPagos'
import PortalCiudadano from './paginas/ciudadano/PortalCiudadano'
import ReportarIncidencia from './paginas/ciudadano/ReportarIncidencia'
import SolicitarTramite from './paginas/ciudadano/SolicitarTramite'
import AtencionIncidencia from './paginas/funcionario/AtencionIncidencia'
import BandejaFuncionario from './paginas/funcionario/BandejaFuncionario'
import CatalogoFuncionario from './paginas/funcionario/CatalogoFuncionario'
import MapaIncidencias from './paginas/funcionario/MapaIncidencias'
import RevisionSolicitud from './paginas/funcionario/RevisionSolicitud'
import VerificarDocumento from './paginas/publico/VerificarDocumento'
import { rutaParaSesion } from './utilidades/rutas'

function Inicio() {
  const { sesion } = useSesion()
  return <Navigate to={rutaParaSesion(sesion)} replace />
}

export default function App() {
  return (
    <SesionProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Inicio />} />

          {/* Acceso */}
          <Route path="/ingresar" element={<RutaPublica><Ingresar /></RutaPublica>} />
          <Route path="/registro" element={<RutaPublica><Registro /></RutaPublica>} />
          <Route path="/recuperar" element={<RutaPublica><Recuperar /></RutaPublica>} />
          <Route path="/restablecer" element={<Restablecer />} />
          <Route path="/verificacion" element={<RutaPaso paso="VERIFICAR_MFA"><Verificacion /></RutaPaso>} />
          <Route path="/vincular-autenticador" element={<RutaPaso paso="CONFIGURAR_MFA"><VincularAutenticador /></RutaPaso>} />
          <Route path="/cambiar-clave" element={<RutaPaso paso="CAMBIAR_CLAVE"><CambiarClave /></RutaPaso>} />

          {/* Ciudadano */}
          <Route path="/portal" element={<RutaRol rol="CIUDADANO"><PortalCiudadano /></RutaRol>} />
          <Route path="/portal/tramites" element={<RutaRol rol="CIUDADANO"><CatalogoTramites /></RutaRol>} />
          <Route path="/portal/tramites/:id" element={<RutaRol rol="CIUDADANO"><SolicitarTramite /></RutaRol>} />
          <Route path="/portal/solicitudes/:id" element={<RutaRol rol="CIUDADANO"><DetalleSolicitud /></RutaRol>} />
          <Route path="/pasarela" element={<RutaRol rol="CIUDADANO"><PasarelaPagos /></RutaRol>} />
          <Route path="/portal/incidencias" element={<RutaRol rol="CIUDADANO"><MisIncidencias /></RutaRol>} />
          <Route path="/portal/incidencias/nueva" element={<RutaRol rol="CIUDADANO"><ReportarIncidencia /></RutaRol>} />
          <Route path="/portal/incidencias/:id" element={<RutaRol rol="CIUDADANO"><DetalleIncidencia /></RutaRol>} />

          {/* Funcionario */}
          <Route path="/consola" element={<RutaRol rol="FUNCIONARIO"><BandejaFuncionario /></RutaRol>} />
          <Route path="/consola/solicitudes/:id" element={<RutaRol rol="FUNCIONARIO"><RevisionSolicitud /></RutaRol>} />
          <Route path="/consola/catalogo" element={<RutaRol rol="FUNCIONARIO"><CatalogoFuncionario /></RutaRol>} />
          <Route path="/consola/incidencias" element={<RutaRol rol="FUNCIONARIO"><MapaIncidencias /></RutaRol>} />
          <Route path="/consola/incidencias/:id" element={<RutaRol rol="FUNCIONARIO"><AtencionIncidencia /></RutaRol>} />

          {/* Administrador */}
          <Route path="/admin" element={<RutaRol rol="ADMIN"><ConsolaAdmin /></RutaRol>} />

          {/* Público */}
          <Route path="/verificar" element={<VerificarDocumento />} />

          <Route path="*" element={<Inicio />} />
        </Routes>
      </BrowserRouter>
    </SesionProvider>
  )
}
