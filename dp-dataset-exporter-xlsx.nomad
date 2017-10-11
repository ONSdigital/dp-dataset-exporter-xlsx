job "dp-dataset-exporter-xlsx" {
  datacenters = ["eu-west-1"]
  region      = "eu"
  type        = "service"

  constraint {
    distinct_hosts = true
  }

  update {
    stagger      = "90s"
    max_parallel = 1
  }

  group "publising" {
    count = "{{PUBLISHING_TASK_COUNT}}"

    constraint {
      attribute = "${node.class}"
      value     = "publishing"
    }

    task "dp-dataset-exporter-xlsx-publishing" {
      driver = "docker"

      artifact {
        source = "s3::https://s3-eu-west-1.amazonaws.com/{{DEPLOYMENT_BUCKET}}/dp-dataset-exporter-xlsx/{{REVISION}}.tar.gz"
      }

      config {
        command = "${NOMAD_TASK_DIR}/start-task"

        args = [
          "java",
          "-Xmx2048m",
          "-jar",
          "dp-dataset-exporter-xlsx.jar",
        ]

        image = "{{ECR_URL}}:concourse-{{REVISION}}"

        port_map {
          http = 22800
        }
      }

      service {
        name = "dp-dataset-exporter-xlsx"
        port = "http"
        tags = ["publishing"]
      }

      resources {
        cpu    = "{{PUBLISHING_RESOURCE_CPU}}"
        memory = "{{PUBLISHING_RESOURCE_MEM}}"

        network {
          port "http" {}
        }
      }

      template {
        source      = "${NOMAD_TASK_DIR}/vars-template"
        destination = "${NOMAD_TASK_DIR}/vars"
      }

      vault {
        policies = ["dp-dataset-exporter-xlsx-publishing"]
      }
    }
  }
}