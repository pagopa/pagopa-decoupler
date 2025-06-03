locals {
  github = {
    org        = "pagopa"
    repository = "pagopa-decoupler"
  }
  prefix = "pagopa"
  product = "${local.prefix}-${var.env_short}"
  project = "${local.prefix}-${var.env_short}-${local.location_short}-${local.domain}"
  runner  = "${local.prefix}-${var.env_short}-${local.location_short}"

  domain = "nodo"
  location_short  = "weu"

  pagopa_apim_name = "${local.product}-apim"
  pagopa_apim_rg   = "${local.product}-api-rg"

  aks_cluster = {
    name                = "${local.product}-${local.location_short}-${var.env}-aks"
    resource_group_name = "${local.product}-${local.location_short}-${var.env}-aks-rg"
  }

  container_app_environment = {
    name           = "${local.prefix}-${var.env_short}-${local.location_short}-github-runner-cae",
    resource_group = "${local.prefix}-${var.env_short}-${local.location_short}-github-runner-rg",
  }
}
