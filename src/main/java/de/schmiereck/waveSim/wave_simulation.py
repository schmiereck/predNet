import numpy as np
import cupy as cp
import cupyx.scipy.signal


class WaveSimulator2D:
 
    """
    Simulates the 2D wave equation
    The system assumes units, where the wave speed is 1.0 pixel/timestep
    source frequency should be adjusted accordingly
    """
    
    def __init__(self, w, h):
        """
        Initialize the 2D wave simulator.
        @param w: Width of the simulation grid.
        @param h: Height of the simulation grid.
        """
        self.global_dampening = 1.0
        self.source_opacity = 0.9       # opacity of source pixels to incoming waves. If the opacity is 0.0
                                        # the field will be completely over written by the source term
                                        # a nonzero value (e.g 0.5) allows for antialiasing of sources to work

        self.c = cp.ones((h, w), dtype=cp.float32)                      # wave speed field (from refractive indices)
        self.d = cp.ones((h, w), dtype=cp.float32)                      # dampening field
        self.u = cp.zeros((h, w), dtype=cp.float32)                     # field values
        self.p = cp.zeros((h, w), dtype=cp.float32)                     # injected field values
        self.u_prev = cp.zeros((h, w), dtype=cp.float32)                # field values of previos frame
        self.ior = cp.zeros((h, w), dtype=cp.float32)                   # Refractive index values for frame
        self.ior_prev = cp.zeros((h, w), dtype=cp.float32)              # Refractive index values for previous frame

        # set boundary dampening to prevent reflections
        self.set_dampening_field(None, 32)

        # Define Laplacian kernel
        # self.laplacian_kernel = cp.array([[0.05, 0.20, 0.05],
        #                                  [0.20, -1.0, 0.20],
        #                                  [0.05, 0.20, 0.05]])

        self.laplacian_kernel = cp.array([[0.103, 0.147, 0.103],
                                          [0.147, -1.0, 0.147],
                                          [0.103, 0.147, 0.103]])

        self.t = 0
        self.dt = 1.0
        # self.sources = cp.zeros([0, 5])

    def reset_time(self):

        """
        Reset the simulation time to zero.
        """
        self.t = 0.0

    def update_field(self):

        """
        Update the simulation field based on the wave equation.
        """
        # calculate laplacian using convolution
        laplacian = cupyx.scipy.signal.convolve2d(self.u, self.laplacian_kernel, mode='same', boundary='fill')

  
        # update field
        v = (self.u - self.u_prev) * self.d
        r = (self.u + v + laplacian * (self.c * self.dt)**2)
      
        self.u_prev[:] = self.u
 
        
        # renormalization 
        sum_u_prev = cp.sum(cp.abs(self.u**2))
        # sum_u_prev_scalar = sum_u_prev.item()
        
        sum_r= cp.sum(cp.abs(r**2))
        # sum_r_scalar = sum_r.item()
        
        
        self.u[:] = 0.997 * cp.sqrt(sum_u_prev / sum_r) * r
        # the factor is used to make the inernal energy losses realistic. 
        # Otherwise renormalization leads to free energy generation in "particle" areas
        """
        
        self.u[:] = r
        """

        self.t += self.dt


    def get_field(self):

        """
        Get the current state of the simulation field.
        @return: A 2D array representing the simulation field.
        """
        return self.u
    
    def get_ior(self):

        """
        Get the current refractive index distribution of the simulation field.
        @return: A 2D array representing the local refractive index.
        """
        return self.ior

    def set_dampening_field(self, d, pml_thickness):

        """
        Set the dampening field for the simulation, which can be used to prevent reflections at boundaries.
        @param d: The dampening field. If None, a default dampening field is applied.
        @param pml_thickness: Thickness of the Perfectly Matched Layer (PML) at the boundaries to prevent reflections.
        """
        if d is not None:
            assert(d.shape == self.d.shape)
            self.d = cp.clip(cp.array(d), 0.0, self.global_dampening)
        else:
            self.d[:] = self.global_dampening

        w = self.d.shape[1]
        h = self.d.shape[0]
        for i in range(pml_thickness):
            v = (i / pml_thickness) ** 0.5
            self.d[i, i:w - i] = v
            self.d[-(1 + i), i:w - i] = v
            self.d[i:h - i, i] = v
            self.d[i:h - i, -(1 + i)] = v


    #def set_speed_field(self, r):
        """
        Set the wave speed in the simulation.
        @param r: The refractive index field.
        """
        #assert(r.shape == self.c.shape)
        #self.c = 1/cp.clip(cp.array(r), 0, 1)
 

    def set_refractive_index_field(self, q):
        """
        Set the initial refractive index field from q, which are values from an 8 bit image file.
        The current values can run from 1 to 25,5 in steps of 0.1. other values are clipped
        """
        assert(q.shape == self.c.shape)
        self.ior = cp.clip(cp.array(q), 0.5, 20)
        self.ior_prev = self.ior
        
    def set_wave_field(self, p):
        """
        Set the initial field in the simulation.
        @param u: The wave field.
        """
        assert(p.shape == self.p.shape)
        self.u = cp.clip(cp.array(p), -1, 1)
        self.u_prev = cp.clip(cp.array(p), -1, 1 )

    def compute_strain_and_new_IOR (self, refractive_persistence, base_IOR_value, non_lin_constant, exponent_value):

        # not good: mixium up ior and speed fields              

        self.refractive_persistence = refractive_persistence  # defines inertia of the medium (0 = no inertia,  > 0 inertia increases)  
        self.base_IOR_value = base_IOR_value        # minimun IOR value for the field = 1 (cannot be very low otherwise speed goes to inf)
        self.non_lin_constant = non_lin_constant    # non-linear constant (alpha)
        self.exponent_value = exponent_value        # non-linear exponent (E)
        

        self.du_dx_kernel = cp.array([[-1, 0.0, 1]])
        self.du_dy_kernel = cp.array([[-1], [0.0], [1]])
        self.wave_speed_field = cp.ndarray
     
        self.du_dx_kernel = cp.array([[-1, 0.0, 1]])
        self.du_dy_kernel = cp.array([[-1], [0.0], [1]])

       # compute net strain in field
        du_dx = cupyx.scipy.signal.convolve2d(self.u, self.du_dx_kernel, mode='same', boundary='fill')
        du_dy = cupyx.scipy.signal.convolve2d(self.u, self.du_dy_kernel, mode='same', boundary='fill')

        self.strain_field = cp.sqrt(du_dx**2 + du_dy**2)
        
        # compute the new refractive index ior_prev + field_strain and make this the new value     
        self.ior =  (refractive_persistence * self.ior_prev  + (base_IOR_value + non_lin_constant * (self.strain_field ** exponent_value))) / (refractive_persistence + 1)
        # self.ior =  ((refractive_persistence - 1) * self.ior_prev / refractive_persistence)  + (1/refractive_persistence) * (base_IOR_value + non_lin_constant * self.strain_field ** exponent_value)


        # self.set_speed_field (self.ior)
        self.c = 1 / self.ior
        self.ior_prev = self.ior 


        # correct the field for the energy that is introduced by increasing the wave speed / spring constant


    def set_sources(self, sources):
        """
        CURRENTLY UNUSED: Set sources for the simulation.
        @param sources: An array of sources, where each source consists of 5 values: x, y, phase, amplitude, frequency.
        """
        assert sources.shape[1] == 5, 'sources must have shape Nx5'
        self.sources = cp.array(sources).astype(cp.float32)

    def update_sources(self):
        """
        CURRENTLY UNUSED:Update the sources in the simulation field based on their properties.
        """
        v = cp.sin(self.sources[:, 2]+self.sources[:, 4]*self.t)*self.sources[:, 3]
        coords = self.sources[:, 0:2].astype(cp.int32)

        t = self.source_opacity
        self.u[coords[:, 1], coords[:, 0]] = self.u[coords[:, 1], coords[:, 0]]*t + v*(1.0-t)

