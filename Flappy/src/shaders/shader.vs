varying vec3 normal;
varying vec3 v;

void main(void)  
{     
   v = vec3(gl_Vertex.x,gl_Vertex.y,gl_Vertex.z);
   normal = normalize(gl_NormalMatrix * gl_Normal);
   gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;  
}