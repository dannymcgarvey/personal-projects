import bpy
import re
bpy.ops.object.select_all()
if len(bpy.context.selected_objects) == 0:
    bpy.ops.object.select_all()
for obj in bpy.context.selected_objects:
    bpy.ops.object.delete()

if bpy.context.screen.is_animation_playing:
    bpy.ops.screen.animation_play()


bpy.data.scenes[0].frame_current = 1
pattern = re.compile("^Cube(\.\d{3})?")
bpy.ops.mesh.primitive_plane_add(location=(0,0,-.545))
bpy.ops.rigidbody.objects_add(type='PASSIVE')
bpy.context.active_object.scale[0] = 2.0
bpy.context.active_object.scale[1] = 2.0

def resize(obj):
    obj.scale[1] = 2.0
    obj.scale[2] = 0.5


def overhang(n):
    if n <= 1 :
        bpy.ops.mesh.primitive_cube_add(location=(0,1.0,0))
        resize(bpy.context.active_object)
    else:
        overhang(n-1)
        for obj in bpy.data.objects:
            if pattern.match(obj.name):
                obj.location[2] += .99
                obj.location[1] += 1/n
        bpy.ops.mesh.primitive_cube_add(location=(0,1/n,0))
        resize(bpy.context.active_object)
        


    bpy.ops.rigidbody.objects_add(type='ACTIVE')


overhang(992)
if not (bpy.context.screen.is_animation_playing):
    bpy.ops.screen.animation_play()


bpy.ops.object.select_all()
