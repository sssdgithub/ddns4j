CREATE TABLE IF NOT EXISTS job_task (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255),
  group_name VARCHAR(255),
  cron_expression VARCHAR(255),
  class_name VARCHAR(255),
  description VARCHAR(255),
  status INT DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS parsing_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    service_provider INT NOT NULL,
    service_provider_id VARCHAR(64),
    service_provider_secret VARCHAR(64),
    record_type INT NOT NULL,
    get_ip_mode INT NOT NULL,
    get_ip_mode_value VARCHAR(255) NOT NULL,
    ip VARCHAR(255) NOT NULL,
    domain VARCHAR(100) NOT NULL,
    state INT NOT NULL,
    update_frequency INT NOT NULL,
    create_date DATETIME,
    update_date DATETIME,
    creator BIGINT,
    updater BIGINT,
    PRIMARY KEY (id)
);
