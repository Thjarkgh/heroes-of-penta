DEPLOY_MODE="dev"
DEPLOY_WEBADMPWD=""
DEPLOY_EMAIL=""
DEPLOY_DOMAIN=""

while [ ! -z "${1}" ]; do
    case ${1} in
        -prod)
            DEPLOY_MODE="prod"
            ;;
        -[dD] | -domain| --domain) shift
            DEPLOY_DOMAIN="${1}"
            ;;
        -[uU] | -user | --user) shift
            SQL_USER="${1}"
            ;;
        -[pP] | -password| --password) shift
            DEPLOY_WEBADMPWD="'${1}'"
            ;;
        -db | -DB | -database| --database) shift
            SQL_DB="${1}"
            ;;
        -[rR] | -del | --del | --delete)
            METHOD=1
            ;;
        *) 
            echo "wrong syntax"
            ;;              
    esac
    shift
done

if [ "${DEPLOY_WEBADMPWD}" = "" ]; then
    echo "no pwd for webadmin"
    exit 1
fi

docker compose -f docker-compose.yaml up -d --remove-orphans
bash deploy/bin/webadmin.sh ${DEPLOY_WEBADMPWD}
bash deploy/bin/webadmin.sh --upgrade
bash deploy/bin/webadmin.sh --mod-secure enable

if [ "${DEPLOY_MODE}" = "prod" ]; then
    if [ "${DEPLOY_EMAIL}" = "" ] || [ "${DEPLOY_DOMAIN}" = "" ]; then
        echo "missing email and/or domain"
        exit 1
    fi

    bash deploy/bin/acme.sh --install --email "${DEPLOY_EMAIL}"
    bash deploy/bin/acme.sh --domain "${DEPLOY_DOMAIN}"
fi
