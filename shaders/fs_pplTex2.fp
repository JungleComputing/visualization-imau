#version 150

in vec3 vertex_pos;
in vec3 vertex_normal;

in vec2 tCoord;

uniform sampler2D my_texture;

uniform float Shininess;

uniform vec4 LightPos;

void main() {
	vec4 matColor = vec4(texture(my_texture, tCoord).rgb, 1.0);
	
	vec3 light_direction = normalize(LightPos.xyz - vertex_pos);	
	vec3 eyespace_normal = normalize(-vertex_pos);
	vec3 reflection = normalize(-reflect(light_direction,vertex_normal)); 
      
	vec4 ambient_factor = vec4(0,0,0,0);//matColor;	
		
	vec4 diffuse_factor = max(dot(vertex_normal, light_direction), 0.0) * matColor;
	diffuse_factor = clamp(diffuse_factor, 0.0, 1.0);
	
 	vec4 specular_factor = pow(max(dot(reflection, eyespace_normal), 0.0), 0.3 * Shininess) * matColor;
 	specular_factor = vec4(0,0,0,0);//clamp(specular_factor, 0.0, 1.0);

	gl_FragColor = ambient_factor + 2*diffuse_factor + specular_factor;
} 

