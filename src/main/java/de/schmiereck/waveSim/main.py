import wave_visualizer as vis
import wave_simulation as sim
import numpy as np
import cupy as cp
import cv2
import math


# field_colormap = vis.get_colormap_lut('icefire', invert=False, black_level=-0.05)

def load_scene_from_image(simulator, scene_image, source_brightness_scale=1.0):
    
    """
    load source from an image description
    The simulation scenes are given as an 8Bit RGB image with the following channel semantics:
        * Red:   The Refractive index times 100 (for refractive index 1.5 you would use value 150)
        * Green: Each pixel with a green value above 0 is a sinusoidal wave source. The green value defines its frequency.
                 WARNING: Do not use anti aliasing for the green channel !
        * Blue:  Absorbtion field. Larger values correspond to higher dampening of the waves, use graduated transitions to avoid reflections
    """
    
    # set refractive index field refractive index value of 50 is equal to IOR = 1 0-255 results in values that can run from 1 to 255/50
    simulator.set_refractive_index_field(scene_image[:, :, 0]/50)
    
    # set wave field
    field_average = 127 / 255           # 127 this is the base value for the wave creation  
    simulator.set_wave_field((scene_image[:, :, 1])/255-field_average)

    # set dampening field
    simulator.set_dampening_field(1.0-scene_image[:, :, 2]/255, 48)

    #  CURRENTLY UNUSED:setting sources
    # sources_pos = np.flip(np.argwhere(scene_image[:, :, 1] > 0), axis=1)
    # phase_amplitude_freq = np.tile(np.array([0, 1.0, 0.3]), (sources_pos.shape[0], 1))
    # sources = np.concatenate((sources_pos, phase_amplitude_freq), axis=1)
    # sources[:, 4] = scene_image[sources_pos[:, 1], sources_pos[:, 0], 1]/255*0.5  # set frequency to channel value
    # simulator.set_sources(sources)

def show_field(field, brightness_scale, field_title):
    gray = (cp.clip(field*brightness_scale, -10, 10) * 127 + 127).astype(np.uint8)
    img = gray.get()
    cv2.imshow(field_title, cv2.cvtColor(img, cv2.COLOR_RGB2BGR))


def main(scene_image_fn, num_iterations, simulation_steps_per_frame, write_videos, z_scaling):

    scene_image = cv2.cvtColor(cv2.imread(scene_image_fn), cv2.COLOR_BGR2RGB)

    # create simulator and visualizer objects

    simulator = sim.WaveSimulator2D(scene_image.shape[1], scene_image.shape[0])
    """
    visualizer = vis.WaveVisualizer(field_colormap=vis.get_colormap_lut('gray', invert=False, black_level=0.0),
                                     intensity_colormap=vis.get_colormap_lut('gray', invert=False, black_level=0.00))
    """
    visualizer = vis.WaveVisualizer(field_colormap=vis.get_colormap_lut('colormap_icefire', invert=False, black_level=-0.05),
                                     intensity_colormap=vis.get_colormap_lut('afmhot', invert=False, black_level=0.1))
    
    # load scene from image file
    load_scene_from_image(simulator, scene_image)

    # create video writers
    if write_videos:
        video_writer1 = cv2.VideoWriter('simulation_field.mp4', cv2.VideoWriter_fourcc(*'mp4v'), 50, (scene_image.shape[1], scene_image.shape[0]))
        video_writer2 = cv2.VideoWriter('simulation_intensity.mp4', cv2.VideoWriter_fourcc(*'mp4v'), 50, (scene_image.shape[1], scene_image.shape[0]))

    # run simulation

    # SET YOUR NON-LINEARITY PARAMETERS HERE:

    refractive_persistence = 0.1    # use only if you want persistence of ROI to avoid fast gradients
    base_IOR_value = 1              # base IOR value
    non_lin_constant = 0.5       # non linear constant alpha
    exponent_value = 4              # stress exponent

    for i in range(num_iterations):

        # simulator.update_sources()
        simulator.update_field()
        simulator.compute_strain_and_new_IOR (refractive_persistence, base_IOR_value, non_lin_constant, exponent_value)
       
        visualizer.update(simulator)

        if i % simulation_steps_per_frame == 0:
 
            frame_int = visualizer.render_intensity(z_scaling)
            frame_field = visualizer.render_field(z_scaling)

            # frame_int = cv2.pyrDown(frame_int)
            # frame_field = cv2.pyrDown(frame_field)

            cv2.imshow("Wave Simulation", frame_field) 
            
            # cv2.resize(frame_int, dsize=(1024, 1024)))
            
            show_field(simulator.ior, 0.5, field_title="Refractive index field")
            # show_field(simulator.strain_field, 1, field_title="Strain field")
            # show_field(simulator.c, 0.5, field_title="Speed field")
            
            cv2.waitKey(1)

            if write_videos:
                video_writer1.write(frame_field)
                video_writer2.write(frame_int)


# SET YOUR SIMULATION PARAMETERS HERE:

if __name__ == "__main__":
    main("current_sim.png", 1000, simulation_steps_per_frame=1, write_videos=True, z_scaling=3)
