# Distributed Image Annotation Service
 
Clients upload photos with a list of keywords; a pool of workers writes those keywords onto the images. 

Distributed Computing course project (MSc, ISEL, 2024/25), deployed on Google Cloud Compute Engine VMs.
 
## How it works
 
1. The client asks the **RegisterServer** (gRPC) which server to use.
2. It streams the photo and keywords to a **SvcServer** (gRPC streaming); the file goes to a shared GlusterFS volume.
3. The server publishes a job to a **RabbitMQ** queue; a **Worker** annotates the image and acknowledges the job.
4. The client downloads the annotated image by ID.
Servers form a **Spread** group that tracks each server's load and elects a leader, which sends new clients to the least-loaded server. If the leader leaves, a new one is elected.
 
**Tech:** Java · gRPC · RabbitMQ · Spread · GlusterFS · Maven · Google Cloud
 
**Authors (group 9):** Douglas Incáo, Beatriz Filipe, Danilo Vieira
