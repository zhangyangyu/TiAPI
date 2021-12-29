# TiAPI (TiDB hackathon 2021 project)

## Authors

@zhangyangyu @Lweb @7houce @haicoder

## Introduction

TiAPI is a standalone service providing restful API to execute SQL statements on TiDB to users. It's an experimental project making TiDB more serviceless. With TiAPI, the real TiDB instances are transparent to end users. This frees users from management duties of database instances and offers more flexibility to the database providers of upgrading and other management things.

## Architecture

![architecture](./architecture.jpg)

## Background

Although TiDB is already a multi-tenant distributed database system, it still follows the traditional instance(cluster) concept. Everyone, no matter what roles they are, users or administrators, has to know the endpoint of each instance to connect correctly, take the duty to monitor and upgrade the cluster, even under the modern traditional cloud service model. Cloud and serverless establish a new model and opportunity to database systems. Under the new model, users have no need to care about management things and service providers could reduce their product versions and environments and make management easier.

To achieve the final goal, TiDB architecture needs to be more cloud native. A rough direction is splitting into more microservices and using more cloud services. This helps reduce costs, enhance scalability and improve security.

However, splitting TiDB functionalities and codebase is not an easy task, especially for hackathon work. So we choose adding another layer, TiAPI, to make a small step first, mimic the final architecture. Actually this might be a promising way, first having a working service and then deleting unnecessary codes from the large codebase.
