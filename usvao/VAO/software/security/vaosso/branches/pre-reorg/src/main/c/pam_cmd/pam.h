#ifndef __PAM_H
#define __PAM_H

#include <security/pam_appl.h>
#ifdef __DARWIN_UNIX03 /* Mac */
#include <security/pam_modules.h>
#else /* Linux */
#include <security/pam_misc.h>
#endif

#endif /* ifndef __PAM_H */
